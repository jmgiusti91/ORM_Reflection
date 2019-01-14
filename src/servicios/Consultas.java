package servicios;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import anotaciones.Columna;
import anotaciones.Id;
import anotaciones.Tabla;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilidades.UBean;
import utilidades.UConexion;

public class Consultas {
    
    private static ArrayList<Field> fields;
    
    private static Tabla tabla;
    
    private static ArrayList<Columna> columnas = new ArrayList<>();
    
    private static Field fieldId = null;
    
    private static void cargarColumnasYId(ArrayList<Field> flds){
        
        for(Field f : flds){
            if(f.getAnnotation(Columna.class) != null){
                columnas.add(f.getAnnotation(Columna.class));
            } else if (f.getAnnotation(Id.class) != null){
                fieldId = f;
            }
	}
        
    }
    
    private static void vaciarColumnasYId(){
        columnas.clear();
        fieldId = null;
    }
	
    public static Object guardar(Object o){
        
        Object id = null;
        
	Connection conn = UConexion.getInstance();
		
	Class clazz = o.getClass();
		
	fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
		
	tabla = (Tabla) clazz.getAnnotation(Tabla.class);
		
	if(tabla != null)
            try {
				
		StringBuilder sbInsert = new StringBuilder();
		sbInsert.append("INSERT INTO " + tabla.nombre() + "(");
		for(Columna c : columnas){
                    sbInsert.append(c.nombre() + ", ");
		}
		sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ") VALUES (");
		for(Field f : fields){
                    try {
                        if(f.getAnnotation(Columna.class) != null){
                            Object valor = UBean.ejecutarGet(o, f.getName());
                            if(valor.getClass().equals(String.class)){
                                sbInsert.append("'").append(valor.toString()).append("'").append(", ");
                            } else {
                                sbInsert.append(valor).append(", ");
                            }
                        }
                    } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException
                                | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
                            }
		}
		sbInsert.replace(sbInsert.length() - 2, sbInsert.length(), ")");
					
                try {
                    PreparedStatement ps = conn.prepareStatement(sbInsert.toString());
                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                PreparedStatement ps2 = conn.prepareStatement("SELECT " + fieldId.getName() + " FROM " + tabla.nombre() + " ORDER BY " + fieldId.getName() + " DESC LIMIT 1");
                ResultSet rs = ps2.executeQuery();
                
                while(rs.next()){
                    id = rs.getObject(1);
                }
					
		conn.close();
            } catch (Exception e) {
		e.printStackTrace();
            };
            vaciarColumnasYId();
            return id;
    }
    
    public static Object obtenerPorId(Class c, Object id){
        
        tabla = (Tabla) c.getAnnotation(Tabla.class);
        
        Object o = null;
        
        Constructor[] constructors = c.getConstructors();
        
        for(Constructor constr : constructors){
            try {
                if(constr.getParameterCount() == 0){
                    o = constr.newInstance();
                }
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
        
        try {
            Connection conn = UConexion.getInstance();
            
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tabla.nombre() + " WHERE " + fieldId.getName() + " = " + id);
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                for(Field f : fields){
                    if(f.getAnnotation(Columna.class) != null){
                        try {
                            UBean.ejecutarSet(o, f.getName(), rs.getObject(f.getAnnotation(Columna.class).nombre()));
                        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            
            try {
                UBean.ejecutarSet(o, fieldId.getName(), id);
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            conn.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        vaciarColumnasYId();
        return o;
    }
    
    public static void modificar(Object o){
        
        tabla = o.getClass().getAnnotation(Tabla.class);
        
        fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
        
        StringBuilder sbSqlModificar = new StringBuilder("UPDATE " + tabla.nombre() + " SET ");
        
        for(Field f : fields){
            if(f.getAnnotation(Columna.class) != null){
                try {
                    sbSqlModificar.append(f.getAnnotation(Columna.class).nombre()).append(" = ").append("'").append(UBean.ejecutarGet(o, f.getName())).append("'").append(", ");
                } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        sbSqlModificar.replace(sbSqlModificar.length() - 2, sbSqlModificar.length(), "");
        
        try {
            sbSqlModificar.append(" WHERE ").append(fieldId.getName()).append(" = ").append(UBean.ejecutarGet(o, fieldId.getName()));
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            Connection conn = UConexion.getInstance();
            System.out.println(conn.isClosed());
            PreparedStatement ps = conn.prepareStatement(sbSqlModificar.toString());
            ps.execute();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        vaciarColumnasYId();
    }
    
    public static void eliminar(Object o){
        
        tabla = o.getClass().getAnnotation(Tabla.class);
        
        fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
        
        try {
            Connection conn = UConexion.getInstance();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tabla.nombre() + " WHERE " + fieldId.getName() + " = " + UBean.ejecutarGet(o, fieldId.getName()));
            ps.execute();
            conn.close();
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        vaciarColumnasYId();
        
    }
    
    public static Object guardarOModificar(Object o){
        
        Object id = null;
        
        tabla = o.getClass().getAnnotation(Tabla.class);
        
        fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
     
        try {
            id = UBean.ejecutarGet(o, fieldId.getName());
            Connection conn = UConexion.getInstance();
            PreparedStatement ps = conn.prepareStatement("SELECT " + fieldId.getName() + " FROM " + tabla.nombre() + " WHERE " + fieldId.getName() + " = " + id);
            ResultSet rs = ps.executeQuery();
            //Object rsObj = rs.getObject(1);
            vaciarColumnasYId();
            if (rs.next()) {
                modificar(o);
            } else {
                id = guardar(o);
            }
            conn.close();
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        vaciarColumnasYId();
        
        return id;
        
    }
    
    public static List<Object> obtenerTodos(Class c){
        
        List<Object> objetos = new ArrayList<>();
    
        tabla = (Tabla) c.getAnnotation(Tabla.class);
        
        Object o = null;
        
        Constructor[] constructors = c.getConstructors();
        
        for(Constructor constr : constructors){
            if(constr.getParameterCount() == 0){
                try {
                    o = constr.newInstance();
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        fields = UBean.obtenerAtributos(o);
        
        cargarColumnasYId(fields);
        
        try {
            Connection conn = UConexion.getInstance();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tabla.nombre());
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                for(Constructor constr : constructors){
                    if(constr.getParameterCount() == 0){
                        try {
                            o = constr.newInstance();
                        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                for(Field f : fields){
                    if(f.getAnnotation(Columna.class) != null) {
                        try {
                            UBean.ejecutarSet(o, f.getName(), rs.getObject(f.getAnnotation(Columna.class).nombre()));
                        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if(f.getAnnotation(Id.class) != null) {
                        try {
                            UBean.ejecutarSet(o, f.getName(), rs.getObject(f.getName()));
                        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                objetos.add(o);
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        vaciarColumnasYId();
        
        return objetos;
        
    }
		
}
