/*
##############################################################################################################
#iGlove v0.1 - designed and developed by Marco Ramilli (http://marcoramilli.blogspot.com) mramilli@gmail.com.#
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


#include <WString.h> //Used for append strings

/* Speed that triggers sensibility*/
#include "WProgram.h"
void setup();
void loop();
const int Speed = 5; // milliseconds to wait before next loop

/* PINS */
const int X = 0; // X:  (Accelerometer M616012)
const int Y = 1; // Y:  (Accelerometer M616012)
const int Z = 2;// Z:  (Accelerometer M616012)
const int right = 3; //right click
const int left = 4; //left click

/*PINS: Default States */
int rightstate = 0; // Default Low (positive logic)
int leftstate = 0; // Default Low (positive locig)

/*Setup section*/
void setup()
{
   Serial.begin(115200); // preparing serial output
   pinMode(right, INPUT);   // preparing  inputs
   pinMode(left, INPUT);   // preparing  inputs
}

/* The Loop, polling each 15 millis*/
void loop()
{ 
  /*  reading fingers */
  rightstate = digitalRead(right);
  leftstate = digitalRead(left);
  
  /* potenziometer actions *It's important the order !!!*  */
  /* Protocol, First the value then the Variable Name */
  Serial.print(analogRead(X));  // First send X,Y,Z coordinates
  Serial.print("X");
  Serial.print(analogRead(Y));
  Serial.print("Y");
  Serial.print(analogRead(Z));
  Serial.print("Z");
  
  /* Second send the eventually clicks */
  if (rightstate == HIGH) {     
      // Right Click    
       Serial.print(0);
  } else if (leftstate == HIGH){
     // Left Click
     Serial.print(1);
  }else{
    // No Clicks
    Serial.print(2);
  }
  Serial.print("F"); // the parameter to send to the application
 
   delay(Speed); // polling
 
}



int main(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

