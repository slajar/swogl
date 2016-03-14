Welcome to the Swogl Wiki. This page is for collecting and summarizing general ideas about Swogl and for discussing solutions to open issues.


# Extensions and improvements #

Providing a good API for a 3D user interface is challenging, and at the moment, there are many aspects which should be improved. The following list may serve as a starting point for further discussion.

  * The classical 2D interaction paradigms are not applicable to 3D. For example: "Resizing" a SwoglComponent could mean to **either** resize the 3D object, **or** to resize the contents while leaving the 3D object at the same size
  * For arbitrarily-shaped SwoglComponents, the arrangement of popups (like for a JComboBox) may hardly be computed in a general way
  * The solution of using hotkeys (like ALT) for switching the interaction modes may not be suitable for some applictions
  * How could/should the camera interaction be provided for users of the library or the end-users?
  * It should be possible to render own 3D geometry in combination with the SwoglComponents
  * (When) should Swogl be ported to use JOGL 2.0?

And of course:

  * All the LayoutManager3D implementations should be more flexible and "adaptive" for an arbitrary number of SwoglComponents of arbitrary size




# Issues #

The following list contains some of the currently known limitations.

**TODO**: These should be described and analyzed more precisely

  * There are still placement problems for JPopupMenus and sub-JMenus
  * Resizing JInternalFrames that are placed on a JDesktopPane does not work
  * There are minor rendering artifacts when movin JInternalFrames
  * There may be rendering artifacts when using JPopupMenus
  * Interaction with recursive SwoglContainers does not work
  * After rotating the camera, the movement in the SpaceLayout does not work properly
  * Swogl currently requires a java.awt.AWTPermissions.listenToAllAWTEvents permission, and may thus not be run as an Applet