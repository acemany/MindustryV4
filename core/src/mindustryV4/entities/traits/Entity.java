package mindustryV4.entities.traits;

import mindustryV4.entities.*;

public interface Entity extends MoveTrait{

    int getID();

    void resetID(int id);

    default void update(){
    }

    default void removed(){
    }

    default void added(){
    }

    default EntityGroup targetGroup(){
        return Entities.defaultGroup();
    }

    @SuppressWarnings("unchecked")
    default void add(){
        targetGroup().add(this);
    }

    @SuppressWarnings("unchecked")
    default void remove(){
        if(getGroup() != null){
            getGroup().remove(this);
        }

        setGroup(null);
    }

    EntityGroup getGroup();

    void setGroup(EntityGroup group);

    default boolean isAdded(){
        return getGroup() != null;
    }
}