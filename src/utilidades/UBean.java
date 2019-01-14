package utilidades;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class UBean {

private static ArrayList<Field> listField = new ArrayList<Field>();
    
    public static ArrayList<Field> obtenerAtributos(Object o){
        listField.clear();
        Class clazz = o.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for(Field f : fields){
            listField.add(f);
        }
        return listField;
    }
    
    public static void ejecutarSet(Object o, String attr, Object valor) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Class clazz = o.getClass();
        Field field = clazz.getDeclaredField(attr);
        String nombreMetodo = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Class[] params = new Class[1];
        params[0] = valor.getClass();
        Method m = clazz.getMethod("set" + nombreMetodo, params);
        Object[] paramsMethod = new Object[1];
        paramsMethod[0] = valor;
        m.invoke(o, paramsMethod);
    }
    
    public static Object ejecutarGet(Object o, String attr) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        Class clazz = o.getClass();
        Field field = clazz.getDeclaredField(attr);
        String nombreMetodo = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        Method m = clazz.getDeclaredMethod("get" + nombreMetodo, new Class[0]);
        Object retorno = m.invoke(o, new Object[0]);
        return retorno;
    }
	
}
