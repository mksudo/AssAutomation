# ASS AUTOMATION

This project aim to recognize conversation text area in PJSK event story record videos and 
automatically generate `.ass` files for the video to add subtitles.  

## Note
This is a rewritten project based on  [Jeunette / ASS-automation-java](https://github.com/Jeunette/ASS-automation-java), 
this project improves runtime, stability, and readability of the previous project, so that further improvements and modifications can made easier.  

## Requirements
This project currently includes `opencv_java460.dll` and `opencv_videoio_ffmpeg460_64.dll` library files in release zip files. The program is written, compiled, 
and tested on windows 10 x64, other platforms and architectures would require different library files.

## How To Use
### General
You will need a `.mp4` video file, recording one PJSK event story, and a `.json` file of that event story from the game assets. The `.json` file can be found at
[pjsek.ai](https://pjsek.ai/assets/ondemand/event_story) thanks to [Erik Chan](https://www.patreon.com/erikchan002).  
If you selected both files, click the run button on the gui to run the program. The gui will present you with the current step of the task and the total tasks. After 
the task completes, a `.ass` file will be created in the same directory of the video file with all parsed data. In case of failure during task, an error message should be 
presented on the gui.
### User Config
#### settings.toml
This file defines user settings, including  
sampleAssFilePath: defines the path to the `.ass` file including headers, styles, and predefined formats, see the `sample.ass` file in the repository.  
resolutionMaskConfigFilePath: defines the path to all the resolution mask files, see below.  
resolutionMaskConfigFileNames: defines all the file names of the resolution mask files. Each resolution mask file defines masks and styles used for text mask and transition 
text mask under a certain resolution.
#### characterStyles.toml
This file defines character styles, the format is `"characterName" = "characterStyleName"`, the names should be wrapped by double quotes so that nothing is escaped.  
There is a default setting, all unknown characters in the story will use the default style.

### Debugging
You can run the `.jar` file in console, there should be some logging information printed. You can also start the `.jar` file with the arg `--debug`, the loggers will use 
properties defined in `logging.properties` and generate a log file, that log file can be used to debug the program.

## Current State Of Development
### Keyframe detector
The keyframe detector will try to parse the video frames in sequential order. If a conversation text is detected, it will try to skip ahead by an estimated amount of frames. 
This makes it generally faster and require less CPU and RAM. However, note that this method is not robust, as if one detection fails, all following detection will also fail, 
but I have tried my best to make the detections accurate enough.
### Multithreading detector
The multithreading detector is not implemented yet and requires more time. Currently it is disable by the code.