/*
 * staticMixer.java
 *
 * Created on 11. mars 2001, 01:49
 */
package neqsim.processSimulation.processEquipment.mixer;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import neqsim.processSimulation.processEquipment.ProcessEquipmentBaseClass;
import neqsim.processSimulation.processEquipment.ProcessEquipmentInterface;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;
import org.apache.logging.log4j.*;

/**
 *
 * @author Even Solbraa
 * @version
 */
public class Mixer extends ProcessEquipmentBaseClass implements ProcessEquipmentInterface, MixerInterface {

    private static final long serialVersionUID = 1000;

    protected ArrayList<StreamInterface> streams = new ArrayList<StreamInterface>(0);
    private int numberOfInputStreams = 0;
    protected Stream mixedStream;
    private boolean isSetOutTemperature = false;
    private double outTemperature = Double.NaN;
    static Logger logger = LogManager.getLogger(Mixer.class);

    /** Creates new staticMixer */
    public Mixer() {
    }

    public Mixer(String name) {
        super(name);
    }

    @Override
	public SystemInterface getThermoSystem() {
        return mixedStream.getThermoSystem();
    }

    @Override
	public void replaceStream(int i, StreamInterface newStream) {
        streams.set(i, newStream);
    }

    @Override
	public void addStream(StreamInterface newStream) {
        streams.add(newStream);

        try {
            if (getNumberOfInputStreams() == 0) {
                mixedStream = (Stream) streams.get(0).clone(); // cloning the first stream
//            mixedStream.getThermoSystem().setNumberOfPhases(2);
//            mixedStream.getThermoSystem().reInitPhaseType();
//            mixedStream.getThermoSystem().init(0);
//            mixedStream.getThermoSystem().init(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        numberOfInputStreams++;
    }

    public StreamInterface getStream(int i) {
        return streams.get(i);
    }

    public void mixStream() {
        int index = 0;
        String compName = new String();
        double lowestPressure = mixedStream.getThermoSystem().getPhase(0).getPressure();
        boolean hasAddedNewComponent = false;
        for (int k = 1; k < streams.size(); k++) {
            if (streams.get(k).getThermoSystem().getPhase(0).getPressure() < lowestPressure) {
                lowestPressure = streams.get(k).getThermoSystem().getPhase(0).getPressure();
                mixedStream.getThermoSystem().getPhase(0).setPressure(lowestPressure);
            }
            for (int i = 0; i < streams.get(k).getThermoSystem().getPhase(0)
                    .getNumberOfComponents(); i++) {

                boolean gotComponent = false;
                String componentName = streams.get(k).getThermoSystem().getPhase(0).getComponent(i)
                        .getName();
                // System.out.println("adding: " + componentName);
                int numberOfPhases = streams.get(k).getThermoSystem().getNumberOfPhases();

                double moles = streams.get(k).getThermoSystem().getPhase(0).getComponent(i)
                        .getNumberOfmoles();
                // System.out.println("moles: " + moles + " " +
                // mixedStream.getThermoSystem().getPhase(0).getNumberOfComponents());
                for (int p = 0; p < mixedStream.getThermoSystem().getPhase(0).getNumberOfComponents(); p++) {
                    if (mixedStream.getThermoSystem().getPhase(0).getComponent(p).getName().equals(componentName)) {
                        gotComponent = true;
                        index = streams.get(0).getThermoSystem().getPhase(0).getComponent(p)
                                .getComponentNumber();
                        compName = streams.get(0).getThermoSystem().getPhase(0).getComponent(p)
                                .getComponentName();

                    }
                }

                if (gotComponent) {
                    // System.out.println("adding moles starting....");
                    mixedStream.getThermoSystem().addComponent(index, moles);
                    // mixedStream.getThermoSystem().init_x_y();
                    // System.out.println("adding moles finished");
                } else {
                    hasAddedNewComponent = true;
                    // System.out.println("ikke gaa hit");
                    mixedStream.getThermoSystem().addComponent(compName, moles);
                }
            }
        }
        if (hasAddedNewComponent)
            mixedStream.getThermoSystem().setMixingRule(mixedStream.getThermoSystem().getMixingRule());
//        mixedStream.getThermoSystem().init_x_y();
//        mixedStream.getThermoSystem().initBeta();
//        mixedStream.getThermoSystem().init(2);
    }

    public double guessTemperature() {
        double gtemp = 0;
        for (int k = 0; k < streams.size(); k++) {
            gtemp += streams.get(k).getThermoSystem().getTemperature()
                    * streams.get(k).getThermoSystem().getNumberOfMoles()
                    / mixedStream.getThermoSystem().getNumberOfMoles();

        }
        return gtemp;
    }

    public double calcMixStreamEnthalpy() {
        double enthalpy = 0;
        for (int k = 0; k < streams.size(); k++) {
            streams.get(k).getThermoSystem().init(3);
            enthalpy += streams.get(k).getThermoSystem().getEnthalpy();
            // System.out.println("total enthalpy k : " + ((SystemInterface) ((Stream)
            // streams.get(k)).getThermoSystem()).getEnthalpy());
        }
        // System.out.println("total enthalpy of streams: " + enthalpy);
        return enthalpy;
    }

    @Override
	public Stream getOutStream() {
        return mixedStream;
    }

    @Override
	public void runTransient() {
        run();
    }

    @Override
	public void run() {
        double enthalpy = 0.0;
        // ((Stream) streams.get(0)).getThermoSystem().display();
        SystemInterface thermoSystem2 = (SystemInterface) streams.get(0).getThermoSystem().clone();

        // System.out.println("total number of moles " +
        // thermoSystem2.getTotalNumberOfMoles());
        mixedStream.setThermoSystem(thermoSystem2);
        // thermoSystem2.display();
        ThermodynamicOperations testOps = new ThermodynamicOperations(thermoSystem2);
        if (streams.size() > 0) {
            mixedStream.getThermoSystem().setNumberOfPhases(2);
            mixedStream.getThermoSystem().reInitPhaseType();
            mixedStream.getThermoSystem().init(0);

            mixStream();

            enthalpy = calcMixStreamEnthalpy();
            // System.out.println("temp guess " + guessTemperature());
            if (!isSetOutTemperature) {
                mixedStream.getThermoSystem().setTemperature(guessTemperature());
            } else {
                mixedStream.setTemperature(outTemperature, "K");
            }
            // System.out.println("filan temp " + mixedStream.getTemperature());
        }
        if (isSetOutTemperature) {
            if (!Double.isNaN(getOutTemperature()))
                mixedStream.getThermoSystem().setTemperature(getOutTemperature());
            testOps.TPflash();
            mixedStream.getThermoSystem().init(2);
        } else {
            try {
                testOps.PHflash(enthalpy, 0);
            } catch (Exception e) {
                logger.error(e.getMessage());
                if (!Double.isNaN(getOutTemperature()))
                    mixedStream.getThermoSystem().setTemperature(getOutTemperature());
                testOps.TPflash();
            }

        }

        // System.out.println("enthalpy: " +
        // mixedStream.getThermoSystem().getEnthalpy());
        // System.out.println("enthalpy: " + enthalpy);
        // System.out.println("temperature: " +
        // mixedStream.getThermoSystem().getTemperature());

        // System.out.println("beta " + mixedStream.getThermoSystem().getBeta());
        // outStream.setThermoSystem(mixedStream.getThermoSystem());
    }

    @Override
	public void displayResult() {
        SystemInterface thermoSystem = mixedStream.getThermoSystem();
        DecimalFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(5);
        nf.applyPattern("#.#####E0");

        JDialog dialog = new JDialog(new JFrame(), "Results from TPflash");
        Container dialogContentPane = dialog.getContentPane();
        dialogContentPane.setLayout(new FlowLayout());

        thermoSystem.initPhysicalProperties();
        String[][] table = new String[50][5];
        String[] names = { "", "Phase 1", "Phase 2", "Phase 3", "Unit" };
        table[0][0] = "";
        table[0][1] = "";
        table[0][2] = "";
        table[0][3] = "";
        StringBuffer buf = new StringBuffer();
        FieldPosition test = new FieldPosition(0);

        for (int i = 0; i < thermoSystem.getNumberOfPhases(); i++) {
            for (int j = 0; j < thermoSystem.getPhases()[0].getNumberOfComponents(); j++) {
                table[j + 1][0] = thermoSystem.getPhases()[0].getComponents()[j].getName();
                buf = new StringBuffer();
                table[j + 1][i + 1] = nf.format(thermoSystem.getPhases()[i].getComponents()[j].getx(), buf, test)
                        .toString();
                table[j + 1][4] = "[-]";
            }
            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 2][0] = "Density";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 2][i + 1] = nf
                    .format(thermoSystem.getPhases()[i].getPhysicalProperties().getDensity(), buf, test).toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 2][4] = "[kg/m^3]";

            // Double.longValue(thermoSystem.getPhases()[i].getBeta());

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 3][0] = "PhaseFraction";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 3][i + 1] = nf
                    .format(thermoSystem.getPhases()[i].getBeta(), buf, test).toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 3][4] = "[-]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 4][0] = "MolarMass";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 4][i + 1] = nf
                    .format(thermoSystem.getPhases()[i].getMolarMass() * 1000, buf, test).toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 4][4] = "[kg/kmol]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 5][0] = "Cp";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 5][i + 1] = nf.format(
                    (thermoSystem.getPhases()[i].getCp() / (thermoSystem.getPhases()[i].getNumberOfMolesInPhase()
                            * thermoSystem.getPhases()[i].getMolarMass() * 1000)),
                    buf, test).toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 5][4] = "[kJ/kg*K]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 7][0] = "Viscosity";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 7][i + 1] = nf
                    .format((thermoSystem.getPhases()[i].getPhysicalProperties().getViscosity()), buf, test).toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 7][4] = "[kg/m*sec]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 8][0] = "Conductivity";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 8][i + 1] = nf
                    .format(thermoSystem.getPhases()[i].getPhysicalProperties().getConductivity(), buf, test)
                    .toString();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 8][4] = "[W/m*K]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 10][0] = "Pressure";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 10][i + 1] = Double
                    .toString(thermoSystem.getPhases()[i].getPressure());
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 10][4] = "[bar]";

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 11][0] = "Temperature";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 11][i + 1] = Double
                    .toString(thermoSystem.getPhases()[i].getTemperature());
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 11][4] = "[K]";
            Double.toString(thermoSystem.getPhases()[i].getTemperature());

            buf = new StringBuffer();
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 13][0] = "Stream";
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 13][i + 1] = name;
            table[thermoSystem.getPhases()[0].getNumberOfComponents() + 13][4] = "-";
        }

        JTable Jtab = new JTable(table, names);
        JScrollPane scrollpane = new JScrollPane(Jtab);
        dialogContentPane.add(scrollpane);
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public void setPressure(double pres) {
        for (int k = 0; k < streams.size(); k++) {
            streams.get(k).getThermoSystem().setPressure(pres);
        }
        mixedStream.getThermoSystem().setPressure(pres);
    }

    public void setTemperature(double temp) {
        for (int k = 0; k < streams.size(); k++) {
            streams.get(k).getThermoSystem().setTemperature(temp);
        }
        mixedStream.getThermoSystem().setTemperature(temp);
    }

    public double getOutTemperature() {
        return outTemperature;
    }

    public void setOutTemperature(double outTemperature) {
        isSetOutTemperature(true);
        this.outTemperature = outTemperature;
    }

    public boolean isSetOutTemperature() {
        return isSetOutTemperature;
    }

    public void isSetOutTemperature(boolean isSetOutTemperature) {
        this.isSetOutTemperature = isSetOutTemperature;
    }

    public int getNumberOfInputStreams() {
        return numberOfInputStreams;
    }

    @Override
	public double getEntropyProduction(String unit) {
        getOutStream().run();
        double entrop = 0.0;
        for (int i = 0; i < numberOfInputStreams; i++) {
            getStream(i).getFluid().init(3);
            entrop += getStream(i).getFluid().getEntropy(unit);
        }
        getOutStream().getThermoSystem().init(3);
        return getOutStream().getThermoSystem().getEntropy(unit) - entrop;
    }
}
