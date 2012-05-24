
# Desc: Device definitions for Activemedia robots.
# Author: Andrew Howard, Richard Vaughan
# Date: 10 Jun 2002
# CVS: $Id: pioneer.inc,v 1.27 2006/02/27 21:35:14 rtv Exp $


# The Pioneer2DX sonar array
define p2dx_sonar ranger
(
  scount 8

  # define the pose of each transducer [xpos ypos heading]
  spose[0] [ 0.075 0.130 90 ]
  spose[1] [ 0.115 0.115 50 ]
  spose[2] [ 0.150 0.080 30 ]
  spose[3] [ 0.170 0.025 10 ]
  spose[4] [ 0.170 -0.025 -10 ]
  spose[5] [ 0.150 -0.080 -30 ]
  spose[6] [ 0.115 -0.115 -50 ]
  spose[7] [ 0.075 -0.130 -90 ]
		
  # define the field of view of each transducer [range_min range_max view_angle]
  sview [0 5.0 15]

  # define the size of each transducer [xsize ysize] in meters
  ssize [0.01 0.05]
)


# define 10 straight bumpers around the edge of the robot
#
# (these angles are correct for p2dx but the offsets are approximate - RTV)
# format: bumper[x] [x y th length radius] (zero radius gives a straight line)
# WARNING: bumpers are not currently supported by Stage>=1.5
define p2dx_bumper bumper
( 
  bcount 10
  bpose[0] [  0.13 -0.185  -65 ]
  bpose[1] [  0.2 -0.11  -24 ]
  bpose[2] [  0.22  0.00    0 ]
  bpose[3] [  0.2  0.11   24 ]
  bpose[4] [  0.13  0.185   65 ]
  bpose[5] [ -0.22  0.185  115 ]
  bpose[6] [ -0.275  0.11  156 ]
  bpose[7] [ -0.30  0.00  180 ]
  bpose[8] [ -0.275 -0.11  206 ]
  bpose[9] [ -0.22 -0.185  245 ]
  blength 0.1
)

# a Pioneer 2 or 3 in standard configuration
define pioneer2dx position
(
  # actual size
  size [0.50 0.37]

  # the pioneer's center of rotation is offset from its center of area
  origin [-0.04 0.0 0]

  # draw a nose on the robot so we can see which way it points
  gui_nose 1

  # estimated mass in KG
  mass 15.0 
	
  # this polygon approximates the shape of a pioneer
  polygons 1
  polygon[0].points 12
  polygon[0].point[0] [  0.26  0.05 ]
  polygon[0].point[1] [  0.205  0.15 ]
  polygon[0].point[2] [  0.13  0.18 ]
  polygon[0].point[3] [ -0.13  0.18 ]
  polygon[0].point[4] [ -0.205  0.15 ]
  polygon[0].point[5] [ -0.26  0.05 ]
  polygon[0].point[6] [ -0.26 -0.05 ]
  polygon[0].point[7] [ -0.205 -0.15 ]
  polygon[0].point[8] [ -0.13 -0.18 ]
  polygon[0].point[9] [  0.13 -0.18 ]
  polygon[0].point[10] [  0.205 -0.15 ]
  polygon[0].point[11] [  0.26 -0.05 ]

  # differential steering model
  drive "diff"

  # uncomment this line if you want to model real pioneers with SICK
  # lasers, where the laser is taller than the robot
  # laser_return 0

  # use the sonar array and bumpersdefined above
  p2dx_sonar()  
  p2dx_bumper()
) 
