package ec.gp.transform;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ec.gp.ERC;
import ec.gp.GPNode;

public abstract class RuleProvider { //TODO - probably implement Prototype
 
    protected GPNode c(Class<?> c, GPNode... children) {
        GPNode res;
        try {
            res = (GPNode) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        // if (res instanceof ERC)
        //     ((ERC)res).resetNode(Evolve. , thread);
        res.children = children;
        for (int i = 0; i < children.length; i++) {
            children[i].parent = res;
            children[i].argposition = (byte)i;
        }
        return res;
    }

    abstract public List<Transform> getRules();

    //NOTE how this field uses getRules during object RuleProvider construction - getRules should not depend on initialization
    protected Map<String, Transform> rulesM = getRules().stream().collect(Collectors.toMap(t -> t.name, t -> t));

    public Transform r(String name) {
        Transform r = rulesM.get(name);
        if (r == null) throw new IllegalStateException(name);
        return r;
    }

}
