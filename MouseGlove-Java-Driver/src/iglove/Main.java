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

/**
 *
 * @author marcoramilli
 */
public class Main {
   //Main method

   //Declares the Arduino object field
   public static ArduinoComm myArduino;

   public static void main(String args[])
   {

      myArduino = new ArduinoComm();

      //Start the arduino connection - usb port - baud rate
      myArduino.start("/dev/tty.usbserial-A7006QXN",115200);

      
   }


}
