/** 
 * Copyright (c) 1998, 2021, Oracle and/or its affiliates. All rights reserved.
 * 
 */


package card;

import javacard.framework.*;
import javacardx.annotations.*;

/**
 * Applet class
 * 
 * @author <user>
 */
@StringPool(value = {
	    @StringDef(name = "Package", value = "card"),
	    @StringDef(name = "AppletName", value = "card")},
	    // Insert your strings here 
	name = "cardStrings")
public class card extends Applet {

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
	
	// code of CLA byte in the command APU header
	final static byte Card_CLA = (byte) 0x80;
	
	// code of INS byte in the command APDU header
	final static byte VERIFY = (byte) 0x20;
	final static byte BEFORE_COMPETITION = (byte) 0x30;
	final static byte BEFORE_EXAM = (byte) 0x40;
	final static byte AFTER_COMPETITION = (byte) 0x50;
	
	// maximum and minimum competition points
	final static short MAX_POINTS = 0x64;
	final static short MIN_POINTS = 0x01;
	
	// maximum and minimum notes
	final static short MAX_NOTE = 0xa;
	final static short MIN_NOTE = 0x1;
	
	// maximum and minimum id
	final static short MAX_ID = 0x5;
	final static short MIN_ID = 0x1;
	
	
    // maximum number of incorrect tries before the
    // PIN is blocked
    final static byte PIN_TRY_LIMIT = (byte) 0x03;
    // maximum size PIN
    final static byte MAX_PIN_SIZE = (byte) 0x08;
	
	
	 // signal that the PIN verification failed
    final static short SW_VERIFICATION_FAILED = 0x6300;
    // signal the the PIN validation is required
    final static short SW_PIN_VERIFICATION_REQUIRED = 0x6301;
    // signal invalid note
    final static short SW_INVALID_NOTE = 0x6A82;
    // signal invalid points
    final static short SW_INVALID_POINTS_AMOUNT = 0x6A83;
    // signal invalid id
    final static short SW_INVALID_ID = 0x6A88;
	
    // signal that the points exceed the maximum
    final static short SW_EXCEED_MAXIMUM_POINTS = 0x6A84;
    // signal the the points becomes negative
    final static short SW_NEGATIVE_POINTS = 0x6A85;
    
    // signal that the note exceed the maximum
    final static short SW_EXCEED_MAXIMUM_NOTE = 0x6A86;
    //signal that the note becomes negative
    final static short SW_NEGATIVE_NOTE = 0x6A87;
    
    // instance variables declaration
    OwnerPIN pin;
    short StudentId = 1;
    
    // classes id, notes, competitions
    byte[] subjectID = {1, 2, 3, 4, 5};
    byte[] notes = {0, 0, 0, 0, 0};
    byte[] day = {0, 0, 0, 0, 0};
    byte[] month = {0, 0, 0, 0, 0};
    byte[] year = {0, 0, 0, 0, 0};
    byte[] competitionID = {1, 2, 3, 4, 5};
    byte[] competitionPoints = {0, 0, 0, 0, 0};
    
    private card(byte[] bArray, short bOffset, byte bLength) {

        // It is good programming practice to allocate
        // all the memory that an applet needs during
        // its lifetime inside the constructor
        pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        byte iLen = bArray[bOffset]; // aid length
        bOffset = (short) (bOffset + iLen + 1);
        byte cLen = bArray[bOffset]; // info length
        bOffset = (short) (bOffset + cLen + 1);
        byte aLen = bArray[bOffset]; // applet data length

        // The installation parameters contain the PIN
        // initialization value
        pin.update(bArray, (short) (bOffset + 1), aLen);
        register();
    }
    
    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        // create a card applet instance
    	new card(bArray, bOffset, bLength);
    }


    @Override
    public boolean select() {
    	if (pin.getTriesRemaining() == 0) {
    		return false;
    	}
    	return true;
    }
    
    @Override
    public void deselect() {
    	pin.reset();
    }
    
    
    
    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     */
    @Override
    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        
        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
        
        // verify the reset of commands have the
        // correct CLA byte, which specifies the
        // command structure
        if (buffer[ISO7816.OFFSET_CLA] != Card_CLA) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        
        switch (buffer[ISO7816.OFFSET_INS]) {
        case BEFORE_COMPETITION:
            beforeCompetition(apdu);
            return;
        case BEFORE_EXAM:
            beforeExam(apdu);
            return;
        case AFTER_COMPETITION:
            afterCompetition(apdu);
            return;
        case VERIFY:
            verify(apdu);
            return;
        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    }
    
    private void beforeCompetition(APDU apdu) {
    	 if (!pin.isValidated()) {
             ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
         }
    	 
    	 byte[] buffer = apdu.getBuffer();
    	 
    	 byte numBytes = buffer[ISO7816.OFFSET_LC];
    	 
    	 byte byteRead = (byte) (apdu.setIncomingAndReceive());
    	 
    	 if ((numBytes != 1) || (byteRead != 1)) {
    		 ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	 }
    	 
    	 // get the competition ID
    	 
    	 byte cmpID = buffer[ISO7816.OFFSET_CDATA];
    	 
    	 // get the competition points
    
    	 byte cmpP = buffer[ISO7816.OFFSET_CDATA + 1];
    	 
    	 // check id
    	 if ((cmpID < MIN_ID) || (cmpID > MAX_ID)) {
    		 ISOException.throwIt(SW_INVALID_ID);
    	 }
    	 
    	 // check points
    	 
    	 if ((cmpP < MIN_POINTS) || (cmpP > MAX_POINTS)) {
    		 ISOException.throwIt(SW_INVALID_POINTS_AMOUNT);
    	 }
    	 
    	 // add points to the COMPETITION
 
    	 competitionPoints[(short)(cmpID - 1)] = cmpP;
    	 
    }
    
    private void beforeExam(APDU apdu) {
    	if	(!pin.isValidated()) {
    		ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
    	}
    	// Receive ID for subject 
    	
    	byte[] buffer = apdu.getBuffer();
    	
    	byte numBytes = buffer[ISO7816.OFFSET_LC];
    	 
    	 byte byteRead = (byte) (apdu.setIncomingAndReceive());
    	 
    	 if ((numBytes != 1) || (byteRead != 1)) {
    		 ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	 }
    	
    	 
    	 if (buffer[ISO7816.OFFSET_CDATA] == 1) {
    		 // Send subject info
    		 byte classId = buffer[ISO7816.OFFSET_CDATA + 1];
    		 
    		 short le = apdu.setOutgoing();	
    	     if (le < 2) {
    	    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	    	}

    	     apdu.setOutgoingLength((byte)6);
    	     
    	     buffer[0] = notes[classId - 1];
    	     buffer[1] = day[classId - 1];
    	     buffer[2] = month[classId - 1];
    	     buffer[3] = year[classId - 1];
    	     buffer[4] = competitionID[classId - 1];
    	     buffer[5] = competitionPoints[classId - 1];
	 
    	     apdu.sendBytes((short) 0, (short) 6);
    	 }
    	 else {
    		 //Update info
    		 byte classId = buffer[ISO7816.OFFSET_CDATA + 1];
    	     notes[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 2];
    	     day[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 3];
    	     month[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 4];
    	     year[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 5];
    	     competitionID[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 6];
    	     competitionPoints[classId - 1] = buffer[ISO7816.OFFSET_CDATA + 4];
    	 }
	
    }
    
    private void afterCompetition(APDU apdu) {
    	if	(!pin.isValidated()) {
    		ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
    	}
    	
    	// Send competition Points
    	
    	byte[] buffer = apdu.getBuffer();
    	
    	short le = apdu.setOutgoing();
    	
    	if (le < 2) {
    		ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	}
    	
    	apdu.setOutgoingLength((byte)5);
    	for (int i = 0; i < competitionPoints.length; i++) {
    		buffer[i] = competitionPoints[i];
    	}
    	
    	apdu.sendBytes((short) 0, (short) 5);
    	
    }
    
    private void verify(APDU apdu) {
    	byte[] buffer = apdu.getBuffer();
        // retrieve the PIN data for validation.
        byte byteRead = (byte) (apdu.setIncomingAndReceive());

        // check pin
        // the PIN data is read into the APDU buffer
        // at the offset ISO7816.OFFSET_CDATA
        // the PIN data length = byteRead
        if (pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead) == false) {
            ISOException.throwIt(SW_VERIFICATION_FAILED);
        }
    }
    
    
}
