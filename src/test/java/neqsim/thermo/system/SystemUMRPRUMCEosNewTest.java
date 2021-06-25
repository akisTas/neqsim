package neqsim.thermo.system;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import neqsim.thermodynamicOperations.ThermodynamicOperations;

class SystemUMRPRUMCEosNewTest {

	static neqsim.thermo.system.SystemInterface testSystem = null;
	static neqsim.thermo.ThermodynamicModelTest testModel = null; 

	@BeforeAll
	public static void setUp(){
		testSystem = new SystemUMRPRUMCEosNew(298.0, 10.0);
		//testSystem = new SystemPrEos(298.0, 10.0);
		testSystem.addComponent("nitrogen", 1);
		//testSystem.addComponent("CO2", 0.01);
		//testSystem.addComponent("methane", 0.68);
		//testSystem.addComponent("ethane", 0.1);
		//testSystem.addComponent("n-heptane", 0.2);
		testSystem.createDatabase(true);
		testSystem.setMixingRule("HV", "UNIFAC_UMRPRU");
		testSystem.init(0);
		testSystem.setNumberOfPhases(1);
		testSystem.init(1);
		testSystem.getPhase(0).getDensity();
		System.out.println("density=kg/m3 " + testSystem.getPhase(0).getDensity() + "kg/m3" );
		System.out.println("pressure " + testSystem.getPressure() + "bar" );
		System.out.println("molar volume " + testSystem.getMolarVolume() + "m3.mol*1e5" );
		
		
		//testModel = new neqsim.thermo.ThermodynamicModelTest(testSystem);
		//ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        //testOps.TPflash();
		
		//testSystem.initProperties();
	}

	@DisplayName("test a calculation")
    public void testcalc() {	 
		testSystem = new SystemUMRPRUMCEosNew(298.0, 10.0);
		testSystem.addComponent("nitrogen", 0.01);
		testSystem.addComponent("CO2", 0.01);
		testSystem.addComponent("methane", 0.68);
		testSystem.addComponent("ethane", 0.1);
		testSystem.addComponent("n-heptane", 0.2);
		testSystem.createDatabase(true);
		testSystem.setMixingRule("HV", "UNIFAC_UMRPRU");
		testModel = new neqsim.thermo.ThermodynamicModelTest(testSystem);
		ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        testOps.TPflash();
		testSystem.initProperties();

        assertEquals(testSystem.getNumberOfPhases(), 2);
	}

	@Test
	@DisplayName("test a TPflash of the fluid (should return two phases)")
    public void testTPflash() {
        assertEquals(testSystem.getNumberOfPhases(), 2);
	}

	@Test
	@DisplayName("test the fugacity coeficients calcutated")
    public void testFugacityCoefficients() {
		assertTrue(testModel.checkFugasityCoeffisients());
	}

	@Test
	@DisplayName("test derivative of fugacity coeficients with respect to pressure")
    public void checkFugasityCoeffisientsDP() {
		assertTrue(testModel.checkFugasityCoeffisientsDP());
	}

	@Test
	@DisplayName("test derivative of fugacity coeficients with respect to temperature")
    public void checkFugasityCoeffisientsDT() {
		assertTrue(testModel.checkFugasityCoeffisientsDT());
	}

	@Test
	@DisplayName("test derivative of fugacity coeficients with respect to composition")
    public void checkFugasityCoeffisientsDn() {
		assertTrue(testModel.checkFugasityCoeffisientsDn());
	}

	@Test
	@DisplayName("test derivative of fugacity coeficients with respect to composition (2nd method)")
    public void checkFugasityCoeffisientsDn2() {
		assertTrue(testModel.checkFugasityCoeffisientsDn2());
	}
	
	//@Disabled
	@Test
	@DisplayName("calculate compressibility of gas phase")
    public void checkCompressibility(){
		System.out.println("gas compressibility " + testSystem.getPhase("gas").getZ());
		assertTrue(testSystem.getPhase("gas").getZ()==0.9711401538454589);
	}

}