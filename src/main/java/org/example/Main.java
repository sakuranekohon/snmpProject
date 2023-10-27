package org.example;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scn = new Scanner(System.in);
        System.out.print("Enter IP Address : ");
        String targetIPAddress = scn.nextLine() + "/161";
        System.out.println("Target IP Address: " + targetIPAddress);
        while (true) {
            System.out.println("Enter OID(ENTER 0 is exit)");
            String oid = scn.nextLine();
            if(oid.equals("0")) break;
            try {
                TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
                Snmp snmp = new Snmp(transport);
                transport.listen();

                CommunityTarget target = new CommunityTarget(GenericAddress.parse(targetIPAddress), new OctetString("public"));
                target.setRetries(2);
                target.setTimeout(1500);
                target.setVersion(SnmpConstants.version2c);

                PDU pdu = new PDU();
                pdu.setType(PDU.GET);
                pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0")));
                pdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.1.0")));
                pdu.add(new VariableBinding(new OID(oid)));
                System.out.print("Sending SNMP request to: " + targetIPAddress + " with OID: ");
                for (var vb:pdu.getVariableBindings().toArray()) {
                    System.out.print(vb+",");
                }
                ResponseEvent responseEvent = snmp.send(pdu, target);
                if (responseEvent.getResponse() != null) {
                    System.out.println("\nReceived response:");
                    VariableBinding[] vbs = responseEvent.getResponse().toArray();
                    for (VariableBinding vb : vbs) {
                        System.out.println(vb.toString());
                    }
                } else {
                    System.out.println("No response received");
                }

                snmp.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}