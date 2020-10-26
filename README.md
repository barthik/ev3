**Warning: This is a re-upload of the project. The original creation date of the project is 2016.**

---

# EV3 Neural Network Controller

The aim of the project is to design a method of control of an autonomous robot using artificial neural networks. The core of the project is the design of the controlling neural network, and generation and filtration of the training set using ART1 (Adaptive Resonance Theory). The outcome of the practical part is an assembled Lego Mindstorms EV3 robot solving the problem of avoiding obstacles in space.

Once you connect the EV3 device, you will see the name of the currently connected EV3 device and the battery status (in %). 

You can also switch off and on the sounds emitted by the device during a state change, set a  motor speed (in degrees / s) and acceleration (in degrees / s / s), see current values measured by the sensors (in cm) and control the device using the keyboard:
 - W / I ... move forward
 - S / K ... move backwards
 - A / J ... turn left
-  D / L ... turn right

The speed can be set from 0 to 600 degrees / s.

Or you can set the parameters of the autonomous movement.

## Related articles:
- [Big Data Filtering Through Adaptive Resonance Theory](https://www.researchgate.net/publication/314071747_Big_Data_Filtering_Through_Adaptive_Resonance_Theory)
- [Control of autonomous robot using neural networks](https://www.researchgate.net/publication/318639124_Control_of_autonomous_robot_using_neural_networks)
- [Control of autonomous robot behavior using data filtering through adaptive resonance theory](https://www.researchgate.net/publication/320818595_Control_of_autonomous_robot_behavior_using_data_filtering_through_adaptive_resonance_theory)

## Application requirements:
- [LeJOS library](https://sourceforge.net/p/lejos/wiki/Home/)

## EV3 robot requirements:
- SD card with [LeJOS (version 0.9.1-beta++)](https://sourceforge.net/projects/ev3.lejos.p/files/)
    - Insert the card into the Micro SD slot in the EV3 programmable block
    - [How to install LeJOS](https://sourceforge.net/p/lejos/wiki/Installing%20leJOS/)
- Left large motor connected to the port B and right large motor connected to the port A
- 4 infrared sensors connected to ports S1, S2, S3 and S4
- The EV3 device connected to the PAN via Bluetooth
