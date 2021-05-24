package javaTerminal;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class Manager {
    public CardTerminal cardTerminal;
    public Database database;
    public Manager(){
        cardTerminal = new CardTerminal();
        database = Database.getInstance();
        database.initDB();
    }

    public void run(){
        //insertStudent();
        establishConnection();
        selectCard();
        while (!verifyUserPin()) {
            System.out.println("Invalid PIN!");
            System.out.println("-------------------");
        }
        while (true){
            System.out.println("Choose option:");
            System.out.println("1.Register competition points");
            System.out.println("2.Note");
            System.out.println("3.Priority update");
            System.out.println("4.Exit");
            String input = "";
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));
                String in = reader.readLine();
                input = in;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            byte[] option = stringToBytes(input);
            switch (option[0]){
                case 1:
                    register();
                    break;
                case 2:
                    note();
                    break;
                case 3:
                    priority();
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Not a valid option!");
                    System.out.println("-------------------");

            }

        }
    }

    private void establishConnection(){
        byte lengthDataField = 20;
        byte maxDataExpected = 0x7F;
        byte instP1 = 0x00;
        byte instP2 = 0x00;

        cardTerminal.establishConnectionToSimulator();
        cardTerminal.pwrUp();
        byte[] cmds = {(byte) 0x80, (byte) 0xB8, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        byte[] data = {(byte) 0x0a,
                (byte)0xa0,
                (byte)0x0,
                (byte)0x0,
                (byte)0x0,
                (byte)0x62,
                (byte)0x3,
                (byte)0x1,
                (byte)0xc,
                (byte)0x6,
                (byte)0x1,
                (byte)0x08,
                (byte)0x0,
                (byte)0x0,
                (byte)0x05,
                (byte)0x01,
                (byte)0x02,
                (byte)0x03,
                (byte)0x04,
                (byte)0x05};
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        cardTerminal.decodeStatus();
        System.out.println("Card applet on!");
        System.out.println("------------------------------");
    }

    private void selectCard(){
        byte lengthDataField = 0x0a;
        byte maxDataExpected = 0x7F;
        byte instP1 = 0x04;
        byte instP2 = 0x00;

        byte[] cmds = {(byte) 0x00, (byte) 0xA4, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        byte[] data = {(byte)0xa0,
                (byte)0x0,
                (byte)0x0,
                (byte)0x0,
                (byte)0x62,
                (byte)0x3,
                (byte)0x1,
                (byte)0xc,
                (byte)0x6,
                (byte)0x1};
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        cardTerminal.decodeStatus();
        System.out.println("Card selected!");
        System.out.println("------------------------------");
    }

    private boolean verifyUserPin() {
        System.out.println("Enter card PIN:");
        String input = "";
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            String in = reader.readLine();
            input = in;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = stringToBytes(input);


//        System.out.println("Test bytes pin:");
//        for (int i=0; i< data.length; i++){
//            System.out.println(atrToHex(data[i]));
//        }

        byte lengthDataField = (byte) data.length;
        byte maxDataExpected = 0x7F;
        byte instP1 = 0x00;
        byte instP2 = 0x00;
        byte[] cmds = {(byte) 0x80, (byte) 0x20, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        byte[] verify = cardTerminal.decodeStatus();

        if (verify[0] == 0x90 && verify[1]==0){
            return false;
        }

        System.out.println("Valid PIN!");
        System.out.println("------------------------------");
        return true;
    }

    private void insertStudent(){
        Document std = new Document("id", "1")
                .append("name", "Vasile Popescu")
                .append("priority", "0")
                .append("subjectName1", "Mate")
                .append("subjectId1", "1")
                .append("subjectNote1", "0")
                .append("subjectNoteDate1", "0")
                .append("subjectCompID1", "1")
                .append("subjectCompPoints1", "0")
                .append("subjectName2", "Info")
                .append("subjectId2", "2")
                .append("subjectNote2", "0")
                .append("subjectNoteDate2", "0")
                .append("subjectCompID2", "2")
                .append("subjectCompPoints2", "0")
                .append("subjectName3", "Fizica")
                .append("subjectId3", "3")
                .append("subjectNote3", "0")
                .append("subjectNoteDate3", "0")
                .append("subjectCompID3", "3")
                .append("subjectCompPoints3", "0")
                .append("subjectName4", "Sport")
                .append("subjectId4", "4")
                .append("subjectNote4", "0")
                .append("subjectNoteDate4", "0")
                .append("subjectCompID4", "4")
                .append("subjectCompPoints4", "0")
                .append("subjectName5", "Chimie")
                .append("subjectId5", "5")
                .append("subjectNote5", "0")
                .append("subjectNoteDate5", "0")
                .append("subjectCompID5", "5")
                .append("subjectCompPoints5", "0");
        database.insertOne(std);
    }

    private void register(){
        ArrayList<String> compId = new ArrayList<String>();
        compId.add("subjectCompID1");
        compId.add("subjectCompID2");
        compId.add("subjectCompID3");
        compId.add("subjectCompID4");
        compId.add("subjectCompID5");
        ArrayList<String> compPns = new ArrayList<String>();
        compPns.add("subjectCompPoints1");
        compPns.add("subjectCompPoints2");
        compPns.add("subjectCompPoints3");
        compPns.add("subjectCompPoints4");
        compPns.add("subjectCompPoints5");
        System.out.println("Enter competition id:");
        String input = "";
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            String in = reader.readLine();
            input = in;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        byte[] cmpId = stringToBytes(input);

        System.out.println("Enter competition points:");
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            String in = reader.readLine();
            input = in;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        byte[] cmpPoints = stringToBytes(input);

        System.out.println("-------------------");

        // Insert in db

        String field = compPns.get(cmpId[0]-1).toString();

        Document filter = new Document(Map.of("id", "1"));
        database.findAndUpdate(filter, combine(set(field, input)));

        // Send to card


        // Insert data to send to card
        byte[] data = new byte[cmpPoints.length + cmpId.length];
        for (int i = 0 ; i < cmpId.length; i++){
            data[i] = cmpId[i];
        }
        for (int i = cmpId.length ; i < cmpPoints.length + cmpId.length; i++){
            data[i] = cmpPoints[i - cmpId.length];
        }

        byte lengthDataField = (byte) data.length;
        byte maxDataExpected = 0x7F;
        byte instP1 = 0x00;
        byte instP2 = 0x00;
        byte[] cmds = {(byte) 0x80, (byte) 0x30, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        cardTerminal.decodeStatus();

        System.out.println("------------------------------");
    }

    private void note(){
        System.out.println("Enter class id:");
        String input = "";
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            String in = reader.readLine();
            input = in;
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        byte[] classId = stringToBytes(input);

        // Send request to get info from card

        byte[] data = {(byte) 1, classId[0]};
        byte lengthDataField = (byte) data.length;
        byte maxDataExpected = 0x7F;
        byte instP1 = 0x00;
        byte instP2 = 0x00;
        byte[] cmds = {(byte) 0x80, (byte) 0x40, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        cardTerminal.decodeStatus();
        byte[] dataOut = cardTerminal.decodeDataOut(); // received from card

        // Check if Competition Points > 80

        if (dataOut[5] > (byte) 80){
            // Insert into db

            // Update card
            byte[] data2 = {(byte) 2, (byte) 10, dataOut[1], dataOut[2], dataOut[3], dataOut[4], (byte) 100};
            lengthDataField = (byte) data2.length;
            cardTerminal.setTheAPDUCommands(cmds);
            cardTerminal.setTheDataLength(lengthDataField);
            cardTerminal.setTheDataIn(data2);
            cardTerminal.setExpctdByteLength(maxDataExpected);
            cardTerminal.exchangeTheAPDUWithSimulator();
            cardTerminal.decodeStatus();

        }

        System.out.println("Update note/competition done!");
        System.out.println("------------------------------");

    }

    private void priority(){
        // send request to card
        byte lengthDataField = 1;
        byte maxDataExpected = 0x05;
        byte instP1 = 0x00;
        byte instP2 = 0x00;
        byte[] data = {(byte)0x7F};
        byte[] cmds = {(byte) 0x80, (byte) 0x50, instP1, instP2};
        cardTerminal.setTheAPDUCommands(cmds);
        cardTerminal.setTheDataLength(lengthDataField);
        cardTerminal.setTheDataIn(data);
        cardTerminal.setExpctdByteLength(maxDataExpected);
        cardTerminal.exchangeTheAPDUWithSimulator();
        cardTerminal.decodeStatus();
        byte[] dataOut = cardTerminal.decodeDataOut(); // received from card

        // update Database

        Document student = database.find(new Document(Map.of("id", "1")));
        Set<Map.Entry<String, Object>> objects = student.entrySet();
        Map<String, Object> mp = new HashMap<>();
        for (var entry: objects) {
            mp.put(entry.getKey(), entry.getValue());
        }

        ArrayList<Object> points= new ArrayList<Object>();
        points.add(mp.get("subjectCompPoints1"));
        points.add(mp.get("subjectCompPoints2"));
        points.add(mp.get("subjectCompPoints3"));
        points.add(mp.get("subjectCompPoints4"));
        points.add(mp.get("subjectCompPoints5"));


        int count = 0;
        for (int i = 0; i < points.size(); i++) {
            if ((int)points.get(i) > 80){
                count = count + 1;
            }
        }

        if (count >= 3){
            Document filter = new Document(Map.of("id", "1"));
            database.findAndUpdate(filter, combine(set("priority", 1)));
        }

        System.out.println(student.entrySet());

        System.out.println("Updated priority!");
        System.out.println("------------------------------");
    }

    private String atrToHex(byte atCode) {
        char hex[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        String str2 = "";
        int num = atCode & 0xff;
        int rem;
        while (num > 0) {
            rem = num % 16;
            str2 = hex[rem] + str2;
            num = num / 16;
        }
        if (str2 != "") {
            return str2;
        } else {
            return "0";
        }
    }

    private byte[] stringToBytes(String s){
        byte[] bytes = new byte[s.length()];
        for (int i=0;i<s.length();++i) {
            char ch = s.charAt(i);
            int intRepr = (int) ch;
            int byteIntRepr = intRepr - (int) '0';
            byte b = (byte) byteIntRepr;
            bytes[i] = b;
        }
        return bytes;
    }
}
