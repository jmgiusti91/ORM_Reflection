/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencia;

import java.util.List;
import servicios.Consultas;
import utilidades.UConexion;

/**
 *
 * @author Juan
 */
public class Main {
    
    public static void main(String[] args) {
        
        UConexion.setUrl("jdbc:mysql://localhost:3306/pa1_personabd");
        
        //Persona p1 = new Persona("Jeremias", "Springfield", 43, true);
        
        //p1.setId(Consultas.guardar(p1));
        
        //System.out.println(p1);
        
        //Persona p2 = (Persona) Consultas.obtenerPorId(Persona.class, 3);
        
        //System.out.println(p2);
        
        //p2.setNombre("Josesito");
        
        //p2.setApellido("Perez");
        
        //Consultas.modificar(p2);
        
        //Consultas.eliminar(p2);
        
        //Persona p3 = new Persona("Jeremias", "Stronger", 33, false);
        
        Persona p3 = (Persona) Consultas.obtenerPorId(Persona.class, 5);
        
        p3.setApellido("Springfield");
        p3.setEdad(43);
        p3.setEsSocio(Boolean.TRUE);
        
        p3.setId((Integer)Consultas.guardarOModificar(p3));
        
        List<Object> objetos = Consultas.obtenerTodos(Persona.class);
        
        System.out.println(objetos);
        
    }
    
}
