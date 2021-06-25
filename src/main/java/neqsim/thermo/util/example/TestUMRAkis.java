package neqsim.thermo.util.example;

import neqsim.thermo.system.SystemUMRPRUMCEosNew;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

public class TestUMRAkis {

    public static void main(String[] args) {
        SystemUMRPRUMCEosNew testSystem = new SystemUMRPRUMCEosNew(298.0, 10.0);
        testSystem.addComponent("methane", 0.);
        testSystem.addComponent("ethane", 0.5);
        testSystem.createDatabase(true);
        testSystem.setMixingRule("HV", "UNIFAC_UMRPRU");

        testSystem.init(0);
        testSystem.init(1);



        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);



        testOps.TPflash();
        testSystem.display();
    }

}
