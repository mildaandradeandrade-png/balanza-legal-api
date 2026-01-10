package com.tuempresa.balanza.component;

import com.tuempresa.balanza.model.CasoLegal;
import com.tuempresa.balanza.repository.CasoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CasoRepository repository;

    public DataSeeder(CasoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            CasoLegal caso1 = new CasoLegal();
            caso1.setPais("Panama");
            caso1.setHechos("Allanamiento en residencia en San Francisco sin orden judicial a las 3:00 AM.");
            caso1.setRama("Penal");
            caso1.setRespuestaConstitucional("### DICTAMEN PANAMÁ ###\n" +
                "PASO 2: Violación del Art. 26 de la Constitución (Inviolabilidad del domicilio).\n" +
                "HOJA DE RUTA: Solicitar nulidad de pruebas bajo la 'doctrina del fruto del árbol ponzoñoso'.");

            CasoLegal caso2 = new CasoLegal();
            caso2.setPais("Panama");
            caso2.setHechos("Retención de salario por encima del límite legal por deuda comercial.");
            caso2.setRama("Laboral");
            caso2.setRespuestaConstitucional("### DICTAMEN PANAMÁ ###\n" +
                "BASE: Código de Trabajo y Art. 70 de la Constitución.\n" +
                "HOJA DE RUTA: Interponer queja ante el MITRADEL por salario inembargable.");

            repository.saveAll(Arrays.asList(caso1, caso2));
            System.out.println("✅ Seeder: Casos de prueba de Panamá cargados exitosamente.");
        }
    }
}