/*
##############################################################################################################
#MouseGlove v0.1 - designed and developed by Marco Ramilli (http://mouseglove.sourceforge.net)               #
##############################################################################################################



##############################################################################################################
# PIN 0 -> Analog X (Accelerometer M616012)                                                                  #
# PIN 1 -> Analog Y (Accelerometer M616012)                                                                  #
# PIN 2 -> Analog Z (Accelerometer M616012)                                                                  #
# PIN 3 -> right Click (pin 3 eventually connect to VCC, pin 3 connected to GND through 10K)                 #
# PIN 4 -> Legt Click (pin 4 eventually connect to VCC, pin 3 connected to GND through 10K)                  #
# Sensor Shield, helps with the cables (3 pins per signal, GND, VCC, Signal .. for semplicity)               #
#                                                                                                            #
# The Accelerometer woeks 1.5g,2g,4g and 6g. ON the board the Gselector on left.                             #
# selector2    selector1    g-range    Senstivity                                                            #
#     0            0          1.5g          800mv/g                                                          #
#     0            1            2g          600mv/g                                                          #
#     1            0            4g          300mv/g                                                          #
#     1            1            6g          200mv/g                                                          #
#                                                                                                            #
#                                                                                                            #
# I decided to control the sensitivity not here into the Firmware but in the Java driver.  This Firmware     #
# reads sensors and send their value to Java Driver.                                                         #
# The implemented protocol follows these rules:                                                              #
#                              x_value:X:y_value:Yz_value:Z(0|1|2):F                                         #
# Where: x,y,z_value are the Analog input and (0|1|2) are the presence of right click (0), left click (1)    #
# or no click (2). X,Y,Z,F are the variable name passed through serial communication                         #
##############################################################################################################
# Term of use:  GPL Licence                                                (c) Copiright 2010 Marco Ramilli  #
##############################################################################################################
*/


package iglove;

//Load Libraries
import com.sun.tools.javadoc.Messager.ExitJavadoc;
import java.io.*;
import java.util.TooManyListenersException;
import java.awt.*;
import java.awt.event.*;

//Load RXTX Library (Serial Communication)
import gnu.io.*;
import javax.management.monitor.Monitor;
import javax.media.j3d.Screen3D;





class ArduinoComm implements SerialPortEventListener
{

   //Used to in the process of converting the read in characters-
   //-first in to a string and then into a number.
   String rawStr ;

   //Declare serial port variable
   SerialPort mySerialPort;

   //Declare input steam
   InputStream in;
   //support
   boolean stop=false;

   // screen dimension
   Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

   /* ### START mouse controls  ### */
   double Xnew,Ynew,Znew; // where the mouse wil be

   double Xold = (dim.width)/2; //keeping track of old position
   double Yold = (dim.height)/2; //keping track of old position
   double Zold = 0; // future use
   double Xs,Ys,Zs; // The real sensor values
   
   //Accelerometer alignment
   int Xalignement = 300; // since sensor values are from 0 to 600/800
   int Yalignement = 400; // since sensor values are from 0 to 600/800
   int Zalignement = 400; //future use

   //Accelerometer sensibility to avoid continuos mouse movement.
   int Xsensibility = 50;
   int Ysensibility = 50;
   int Zsensibility = 50; //future use

   //clicks controls
   int clickPresent; // 0 right 1 left
   int clickPast =2; // Past click, in this way we know what button to release
   Robot robo; // just java API (mouse control)
   /* ### STOP mouse controls  ### */

   // This parameter is really important to send the command to robo at the right time
   int numberOfParameter = 4; // 4 parameters has been passed from the hardware X:Y:Z:F

   /* Constructor */
    public  ArduinoComm(){
        super();
        try{

            rawStr=""; //support string initialization
            robo = new Robot(); //commanding mouse

        }catch(Exception e){System.out.println("ERROR: "+e);}
    }



    
   //This open's the communcations port with the arduino
   public void start(String portName,int baudRate)
   {
      // just the collector flag
      stop=false;
      
      try
      {
         //Finds and opens the port
         CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
         mySerialPort = (SerialPort)portId.open("my_java_serial" + portName, 2000);
         System.out.println("Serial port found and opened");

         //configure the port
         try
         {
            mySerialPort.setSerialPortParams(baudRate,
            mySerialPort.DATABITS_8,
            mySerialPort.STOPBITS_1,
            mySerialPort.PARITY_NONE);
            System.out.println("Serial port params set: "+baudRate);
         }
         catch (UnsupportedCommOperationException e)
         {
            System.out.println("Probably an unsupported Speed");
         }

         //establish stream for reading from the port
         try
         {
            in = mySerialPort.getInputStream();
         }
         catch (IOException e)
         {
            System.out.println("couldn't get streams");
         }

         // we could read from "in" in a separate thread, but the API gives us events
         try
         {
            mySerialPort.addEventListener(this);
            mySerialPort.notifyOnDataAvailable(true);
            System.out.println("Event listener added");
         }
         catch (TooManyListenersException e)
         {
            System.out.println("couldn't add listener");
         }
      }
      catch (Exception e)
      {
         System.out.println("Port in Use: "+e);
      }
   }


   //Used to close the serial port
   public void closeSerialPort()
   {
      try
      {
         in.close();
         stop=true;
         mySerialPort.close();
         System.out.println("Serial port closed");

      }
      catch (Exception e)
      {
      System.out.println(e);
      }
   }


   // Move mouse
   public void setMouse(){
       scale();
      this.robo.mouseMove( (int)this.Xnew, (int)this.Ynew);
       //this.robo.mouseMove( dim.width/2, (int)this.Ynew);
       //this.robo.mouseMove( (int)this.Xnew, dim.height/2);
   }

   /* This Function Modifies the coordinates to fit better the mouse experience*/
   private void scale(){

       // X calculation
       if(this.Xs >= this.Xsensibility + this.Xalignement)
               this.Xnew = this.Xold + 1;
       if(this.Xs  < this.Xalignement - this.Xsensibility)
               this.Xnew = this.Xold - 1;
       if(this.Xs >= this.Xalignement  - this.Xsensibility && this.Xs <= this.Xsensibility - this.Xalignement)
               this.Xnew = this.Xold + 0;

       // X Out of bound control
       if (this.Xnew >= dim.width )
           this.Xnew = dim.width;

       if (this.Xnew <= 0 )
           this.Xnew = 0;

        // Y calculation
       if(this.Ys > this.Ysensibility + this.Yalignement)
               this.Ynew = this.Yold + 1;
       if(this.Ys  < this.Yalignement - this.Ysensibility)
               this.Ynew = this.Yold - 1;
       if(this.Ys >=  this.Yalignement - this.Ysensibility && this.Ys  <= this.Ysensibility - this.Yalignement)
               this.Ynew = this.Yold + 0;

       // Y Out of bound control
       if (this.Ynew >= dim.height )
           this.Ynew = dim.height;

       if (this.Ynew <= 0 )
           this.Ynew = 0;


       //tracking the current position
       this.Xold = this.Xnew;
       this.Yold = this.Ynew;
       this.Zold = this.Znew; //future

       // Debug
       System.out.println("-> X pointer :" + this.Xnew);
       System.out.println("-> Y pointer :" + this.Ynew);



   }

   // Right Click Press
   private void RightClickPRESS(){
        this.robo.mousePress(InputEvent.BUTTON1_MASK);

   }
   // Right Click Release
   private void RightClickRELEASE(){
        this.robo.mouseRelease(InputEvent.BUTTON1_MASK);

   }

    // Left Click Press
   private void LeftClickPRESS(){
       this.robo.mousePress(InputEvent.BUTTON3_MASK);
   
   }
   // Left Click Release
   private void LeftClickRELEASE(){
       this.robo.mouseRelease(InputEvent.BUTTON3_MASK);
   
   }


   // Serial Handler.
   public void serialEvent(SerialPortEvent event)
   {


      //Reads in data while data is available
      while (event.getEventType()== SerialPortEvent.DATA_AVAILABLE && stop==false)
      {
         try
         {
         if ( this.numberOfParameter  == 0 ){
             // this means that I have X,Y,Z and Finger in the variables
             System.out.println("From Hardware Xsensor: "+this.Xs+" Ysensor: "+this.Ys+" Zsensor: "+this.Zs+" Fingher: "+this.clickPresent);
             // moving the mouse
             this.setMouse();

             //click ?
             switch(this.clickPresent){
                 case 0:
                     this.RightClickPRESS();
                     this.clickPast=0;
                     break;
                 case 1:
                     this.LeftClickPRESS();
                     this.clickPast=1;
                     break;
                 case 2:
                     switch(this.clickPast){
                         case 0:
                             this.RightClickRELEASE();
                             this.clickPast = 2;
                             break;
                         case 1:
                             this.LeftClickRELEASE();
                             this.clickPast = 2;
                             break;
                     }
                     break;


             }//case

             

             this.numberOfParameter = 4;
         }


             //-------------------------------------------------------------------

            //Read in the available character
            char ch = (char)in.read();

            //If the read character is a letter this means that we have found an identifier.
            if (Character.isLetter(ch)==true && rawStr!="")
            {
               //Convert the string containing the characters accumulated since the last identifier into a double.
               double value = Double.parseDouble(rawStr);
                // these IF save the hardware value (Sensors value)
		if (ch=='X')this.Xs = value;
		if (ch=='Y')this.Ys = value; 
                if (ch=='Z')this.Zs = value;
                if (ch=='F')this.clickPresent = (int)value;




               //Reset rawStr ready for the next reading
               rawStr = ("");

            this.numberOfParameter --;
            }
            else
            {
               //Add incoming characters to a string.
               //Only add characters to the string if they are digits.
               //When the arduino starts up the first characters it sends through are S-t-a-r-t-
               //and so to avoid adding these characters we only add characters if they are digits.

               if (Character.isDigit(ch))
               {
                  rawStr = ( rawStr + Character.toString(ch));
               }
               else
               {
                  //Get the decimal point
                  if (ch=='.')
                  {
                     rawStr = ( rawStr + Character.toString(ch));
                  }
                  else
                  {
                     System.out.print(ch);
                  }
               }
            }
         }//try
         catch (Exception e)
         {
             System.out.println("Error" + e);
         }

         
      }//close while
   }

}
