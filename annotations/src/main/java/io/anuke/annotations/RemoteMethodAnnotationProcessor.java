package io.anuke.annotations;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.annotations.IOFinder.ClassSerializer;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.util.*;
import java.util.stream.Collectors;


/** The annotation processor for generating remote method call code. */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
    "io.anuke.annotations.Annotations.Remote",
    "io.anuke.annotations.Annotations.WriteClass",
    "io.anuke.annotations.Annotations.ReadClass",
})
public class RemoteMethodAnnotationProcessor extends AbstractProcessor{
    /** Maximum size of each event packet. */
    public static final int maxPacketSize = 4096;
    /** Name of the base package to put all the generated classes. */
    private static final String packageName = "mindustryV4.gen";

    /** Name of class that handles reading and invoking packets on the server. */
    private static final String readServerName = "RemoteReadServer";
    /** Name of class that handles reading and invoking packets on the client. */
    private static final String readClientName = "RemoteReadClient";
    /**Simple class name of generated class name.*/
    private static final String callLocation = "Call";

    /** Processing round number. */
    private int round;

    //class serializers
    private HashMap<String, ClassSerializer> serializers;
    //all elements with the Remote annotation
    private Set<? extends Element> elements;
    //map of all classes to generate by name
    private HashMap<String, ClassEntry> classMap;
    //list of all method entries
    private ArrayList<MethodEntry> methods;
    //list of all method entries
    private ArrayList<ClassEntry> classes;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv){
        super.init(processingEnv);
        //put all relevant utils into utils class
        Utils.typeUtils = processingEnv.getTypeUtils();
        Utils.elementUtils = processingEnv.getElementUtils();
        Utils.filer = processingEnv.getFiler();
        Utils.messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        if(round > 1) return false; //only process 2 rounds

        round++;

        try{

            //round 1: find all annotations, generate *writers*
            if(round == 1){
                //get serializers
                serializers = new IOFinder().findSerializers(roundEnv);
                //last method ID used
                int lastMethodID = 0;
                //find all elements with the Remote annotation
                elements = roundEnv.getElementsAnnotatedWith(Remote.class);
                //map of all classes to generate by name
                classMap = new HashMap<>();
                //list of all method entries
                methods = new ArrayList<>();
                //list of all method entries
                classes = new ArrayList<>();

                List<Element> orderedElements = new ArrayList<>(elements);
                orderedElements.sort(Comparator.comparing(Object::toString));

                //create methods
                for(Element element : orderedElements){
                    Remote annotation = element.getAnnotation(Remote.class);

                    //check for static
                    if(!element.getModifiers().contains(Modifier.STATIC) || !element.getModifiers().contains(Modifier.PUBLIC)){
                        Utils.messager.printMessage(Kind.ERROR, "All @Remote methods must be public and static: ", element);
                    }

                    //can't generate none methods
                    if(annotation.targets() == Loc.none){
                        Utils.messager.printMessage(Kind.ERROR, "A @Remote method's targets() cannot be equal to 'none':", element);
                    }

                    //get and create class entry if needed
                    if(!classMap.containsKey(callLocation)){
                        ClassEntry clas = new ClassEntry(callLocation);
                        classMap.put(callLocation, clas);
                        classes.add(clas);

                        Utils.messager.printMessage(Kind.NOTE, "Generating class '" + clas.name + "'.");
                    }

                    ClassEntry entry = classMap.get(callLocation);

                    //create and add entry
                    MethodEntry method = new MethodEntry(entry.name, Utils.getMethodName(element), annotation.targets(), annotation.variants(),
                            annotation.called(), annotation.unreliable(), annotation.forward(), lastMethodID++, (ExecutableElement) element, annotation.priority());

                    entry.methods.add(method);
                    methods.add(method);
                }

                //create read/write generators
                RemoteWriteGenerator writegen = new RemoteWriteGenerator(serializers);

                //generate the methods to invoke (write)
                writegen.generateFor(classes, packageName);

                return true;
            }else if(round == 2){ //round 2: generate all *readers*
                RemoteReadGenerator readgen = new RemoteReadGenerator(serializers);

                //generate server readers
                readgen.generateFor(methods.stream().filter(method -> method.where.isClient).collect(Collectors.toList()), readServerName, packageName, true);
                //generate client readers
                readgen.generateFor(methods.stream().filter(method -> method.where.isServer).collect(Collectors.toList()), readClientName, packageName, false);

                //create class for storing unique method hash
                TypeSpec.Builder hashBuilder = TypeSpec.classBuilder("MethodHash").addModifiers(Modifier.PUBLIC);
                hashBuilder.addField(FieldSpec.builder(int.class, "HASH", Modifier.STATIC, Modifier.PUBLIC, Modifier.FINAL)
                        .initializer("$1L", Objects.hash(methods)).build());

                //build and write resulting hash class
                TypeSpec spec = hashBuilder.build();
                JavaFile.builder(packageName, spec).build().writeTo(Utils.filer);

                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return false;
    }
}
