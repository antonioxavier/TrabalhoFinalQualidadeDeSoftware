package cz.cvut.kbss.termit.model.util;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public interface Types {

	Set<String> getTypes();
	
    void setTypes(Set<String> types);
    


    
    default void removeType(String type) {
        Objects.requireNonNull(type);
        if (getTypes() == null) {
            return;
        }
        getTypes().remove(type);
    }
    
    
    default boolean hasType(String type) {
        if (getTypes() == null) {
            return false;
        }
        return getTypes().contains(type);
    }

}