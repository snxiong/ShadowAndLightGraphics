package a3;

import graphicslib3D.*;
import graphicslib3D.light.*;
import graphicslib3D.GLSLUtils.*;
import graphicslib3D.shape.*;

import java.io.File;
import java.nio.*;
import javax.swing.*;

import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import a3.Sphere;
import a3.backwardAction;
import a3.downArrowAction;
import a3.downwardAction;
import a3.errorChecker;
import a3.forwardAction;
import a3.leftAction;
import a3.leftArrowAction;
import a3.rightAction;
import a3.rightArrowAction;
import a3.spaceAction;
import a3.upArrowAction;
import a3.upwardAction;

import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

public class Starter extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	private Material thisMaterial;
	private String[] vBlinn1ShaderSource, vBlinn2ShaderSource, fBlinn2ShaderSource;
	private int rendering_program1, rendering_program2;
	private int vao[] = new int[1];
	private int vbo[] = new int[14];
	private int mv_location, proj_location, n_location;
	private float aspect;
	private GLSLUtils util = new GLSLUtils();
	
	errorChecker shaderChecker = new errorChecker();
	
	// location of torus, pyramid, sphere, and axis
	private Point3D torusLoc = new Point3D(1.6, 0.0, -0.3);
	private Point3D pyrLoc = new Point3D(-1.0, 0.1, 0.3);
	private Point3D sphereLoc = new Point3D(-3.8f, 2.2f, 1.1f);
	private Point3D axisLoc = new Point3D(0.0, 0.0, 0.0);
	
	// view matrix
	private Matrix3D m_matrix = new Matrix3D();
	private Matrix3D v_matrix = new Matrix3D();
	private Matrix3D mv_matrix = new Matrix3D();
	private Matrix3D proj_matrix = new Matrix3D();
	
	private Camera Cam = new Camera();	// Camera 
	private Light lightSource = new Light();	// light source
	
	private Sphere mySphere = new Sphere(24);
	private Point3D sphereObjLoc = new Point3D(3.0, 0.5, 0.5);
	
	
	
	// light stuff
	private float [] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	private PositionalLight currentLight = new PositionalLight();
	private PositionalLight onCurrentLight = currentLight;
	private PositionalLight offCurrentLight = new PositionalLight();
	
	// for turning the lights off
	float[] amb4 = new float[] {0.0f, 0.0f, 0.0f, 0.0f};
	float[] dif4 = new float[] {0.0f, 0.0f, 0.0f, 0.0f};
	float[] spec4 = new float[] {0.0f, 0.0f, 0.0f, 0.0f};
	
	
	
	// shadow stuff
	private int scSizeX, scSizeY;
	private int [] shadow_tex = new int[1];
	private int [] shadow_buffer = new int[1];
	private Matrix3D lightV_matrix = new Matrix3D();
	private Matrix3D lightP_matrix = new Matrix3D();
	private Matrix3D shadowMVP1 = new Matrix3D();
	private Matrix3D shadowMVP2 = new Matrix3D();
	private Matrix3D b = new Matrix3D();

	// model stuff
	private ImportedModel pyramid = new ImportedModel("pyr.obj");
	private Torus myTorus = new Torus(0.6f, 0.4f, 48);
	private int numPyramidVertices, numTorusVertices;
	
	// VARIABLES TO HOLD THE DEGREE OF HOW MUCH TO PAN THE CAMERA
	private static double panDegreeUpDown = 0.0;
	private static double panDegreeRightLeft = 0.0;
	
	// texture variables
	private int iceTexture;
	private Texture joglIceTexture;
	
	private int brickTexture;
	private Texture joglBrickTexture;
	
	private int earthTexture;
	private Texture joglEarthTexture;
	
	private int redTexture;
	private Texture joglRedTexture;
	
	private int blueTexture;
	private Texture joglBlueTexture;
	
	private int greenTexture;
	private Texture joglGreenTexture;
	
	spaceAction spaceListener = new spaceAction();
	lightSwitchAction lightSwitchListener = new lightSwitchAction();
	
	
	
	public Starter()
	{	setTitle("CSC155 Assignment#3 Steven Xiong");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		getContentPane().add(myCanvas);
		setVisible(true);
		
		// Codes to help with KEY PRESSING ACTION
		JComponent contentPane = (JComponent) this.getContentPane();
		int mapName = JComponent.WHEN_IN_FOCUSED_WINDOW;
		InputMap imap = contentPane.getInputMap(mapName);
				
		// settings up 'w' key press	MOVE FORWARD
		KeyStroke wKey = KeyStroke.getKeyStroke('w');
		imap.put(wKey, "moveForward");
		
		forwardAction forwardListener = new forwardAction(Cam);
		ActionMap forwardKey = contentPane.getActionMap();
		forwardKey.put("moveForward", forwardListener);
		this.requestFocus();
		// end of the 'w' key press 
		
		// settings up 's' key press	MOVE BACKWARD
		KeyStroke sKey = KeyStroke.getKeyStroke('s');
		imap.put(sKey, "moveBackward");
		
		backwardAction backwardListener = new backwardAction(Cam);
		ActionMap backwardKey = contentPane.getActionMap();
		backwardKey.put("moveBackward", backwardListener);
		this.requestFocus();
		// end of the 's' key press
		 
		// setting up 'd' key press		MOVE RIGHT
		KeyStroke dKey = KeyStroke.getKeyStroke('d');
		imap.put(dKey, "moveRight");
		
		rightAction rightListener = new rightAction(Cam);
		ActionMap rightKey = contentPane.getActionMap();
		rightKey.put("moveRight", rightListener);
		this.requestFocus();
		// end of the 'd' key press
		
		// setting up 'a' key press		MOVE LEFT
		KeyStroke aKey = KeyStroke.getKeyStroke('a');
		imap.put(aKey, "moveLeft");
		
		leftAction leftListener = new leftAction(Cam);
		ActionMap leftKey = contentPane.getActionMap();
		leftKey.put("moveLeft", leftListener);
		this.requestFocus();
		// end of 'a' key press
		
		// setting up 'q' key press		MOVE UP
		KeyStroke qKey = KeyStroke.getKeyStroke('q');
		imap.put(qKey, "moveUp");
		
		upwardAction upwardListener = new upwardAction(Cam);
		ActionMap upKey = contentPane.getActionMap();
		upKey.put("moveUp", upwardListener);
		this.requestFocus();
		// end of 'q' key press
		
		// setting up 'e' key press		MOVE DOWN
		KeyStroke eKey = KeyStroke.getKeyStroke('e');
		imap.put(eKey, "moveDown");
		
		downArrowAction downArrowListener = new downArrowAction(Cam);
		downwardAction downwardListener = new downwardAction(Cam);
		ActionMap downKey = contentPane.getActionMap();
		downKey.put("moveDown", downwardListener);
		this.requestFocus();
		// end of 'e' key press
		
		/////////////////////////////////////////////////////////////
		///			CAMERA PANNING KEY PRESSES		/////////////////
		/////////////////////////////////////////////////////////////
		
		// setting up UPARROW key press
		KeyStroke upPanKey = KeyStroke.getKeyStroke("UP");
		imap.put(upPanKey, "panUp");
		
		upArrowAction upArrowListener = new upArrowAction(Cam);
		ActionMap panUpKey = contentPane.getActionMap();
		panUpKey.put("panUp", upArrowListener);
		this.requestFocus();
		// end of UPARROW key press
		
		// setting up DOWNARROW key press
		KeyStroke downPanKey = KeyStroke.getKeyStroke("DOWN");
		imap.put(downPanKey, "panDown");
		
		ActionMap panDownKey = contentPane.getActionMap();
		panDownKey.put("panDown", downArrowListener);
		this.requestFocus();
		// end of DOWNARROW key press
		
		// setting up RIGHTARROW key press
		KeyStroke rightPanKey = KeyStroke.getKeyStroke("RIGHT");
		imap.put(rightPanKey, "panRight");
		
		rightArrowAction rightArrowListener = new rightArrowAction(Cam);
		ActionMap panRightKey = contentPane.getActionMap();
		panRightKey.put("panRight", rightArrowListener);
		this.requestFocus();
		// end of RIGHTARROW key press
		
		// setting up LEFTARROW key press
		KeyStroke leftPanKey = KeyStroke.getKeyStroke("LEFT");
		imap.put(leftPanKey, "panLeft");
		
		leftArrowAction leftArrowListener = new leftArrowAction(Cam);
		ActionMap panLeftKey = contentPane.getActionMap();
		panLeftKey.put("panLeft", leftArrowListener);
		this.requestFocus();
		// end of LEFTARROW key press
		
		/////////////////////////////////////////////////////////////
		///			LIGHT MOVEMENT KEY PRESSES		/////////////////
		/////////////////////////////////////////////////////////////
		
		//setting forward light 'i' key press
		KeyStroke iKey = KeyStroke.getKeyStroke('i');
		imap.put(iKey, "moveLightForward");
		
		lightForwardAction lightForwardListener = new lightForwardAction(lightSource);
		ActionMap lightFKey = contentPane.getActionMap();
		lightFKey.put("moveLightForward", lightForwardListener);
		this.requestFocus();
		// end of 'i' key press
		
		// setting backward light 'k' key press
		KeyStroke kKey = KeyStroke.getKeyStroke('k');
		imap.put(kKey, "moveLightBackward");
		
		lightBackwardAction lightBackwardListener = new lightBackwardAction(lightSource);
		ActionMap lightBKey = contentPane.getActionMap();
		lightBKey.put("moveLightBackward", lightBackwardListener);
		this.requestFocus();
		// end of 'k' key press
		
		//setting leftward light 'j' key press
		KeyStroke jKey = KeyStroke.getKeyStroke('j');
		imap.put(jKey, "moveLightLeft");
		
		lightLeftAction lightLeftListener = new lightLeftAction(lightSource);
		ActionMap lightLKey = contentPane.getActionMap();
		lightLKey.put("moveLightLeft", lightLeftListener);
		this.requestFocus();
		// end of 'j' key press
		
		//setting rightward light 'l' key press
		KeyStroke lKey = KeyStroke.getKeyStroke('l');
		imap.put(lKey, "moveLightRight");
		
		lightRightAction lightRightListener = new lightRightAction(lightSource);
		ActionMap lightRKey = contentPane.getActionMap();
		lightRKey.put("moveLightRight", lightRightListener);
		this.requestFocus();
		// end of 'l' key press
		
		// setting upward light 'i' key press
		KeyStroke uKey = KeyStroke.getKeyStroke('u');
		imap.put(uKey, "moveLightUpward");
		
		lightUpwardAction lightUpwardListener = new lightUpwardAction(lightSource);
		ActionMap lightUKey = contentPane.getActionMap();
		lightUKey.put("moveLightUpward", lightUpwardListener);
		this.requestFocus();
		// end of 'u' key press
		
		// setting downward 'o' key press
		KeyStroke oKey = KeyStroke.getKeyStroke('o');
		imap.put(oKey, "moveLightDownward");
		
		lightDownwardAction lightDownwardListener = new lightDownwardAction(lightSource);
		ActionMap lightOKey = contentPane.getActionMap();
		lightOKey.put("moveLightDownward", lightDownwardListener);
		this.requestFocus();
		// end of 'o' key press
		
		// setting up SPACE key press
		KeyStroke spaceKey = KeyStroke.getKeyStroke("SPACE");
		imap.put(spaceKey, "spaceAxes");
		
		ActionMap spaceAxesKey = contentPane.getActionMap();
		spaceAxesKey.put("spaceAxes", spaceListener);
		this.requestFocus();
		// end of SPACE key press
		
		// setting up 't' key press
		KeyStroke tKey = KeyStroke.getKeyStroke('t');
		imap.put(tKey, "lightSwitch");
		
		lightSwitchAction lightSwitchListener = new lightSwitchAction();
		ActionMap lightSWKey = contentPane.getActionMap();
		lightSWKey.put("lightSwitch", lightSwitchListener);
		this.requestFocus();
		// end of 't' key press
	
		
		FPSAnimator animator = new FPSAnimator(myCanvas, 50);
		animator.start();
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		currentLight.setPosition(lightSource.getLight());
		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		proj_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);
		
		float bkg[] = { 0.0f, 0.0f, 0.0f, 1.0f };
		FloatBuffer bkgBuffer = Buffers.newDirectFloatBuffer(bkg);
		gl.glClearBufferfv(GL_COLOR, 0, bkgBuffer);

		gl.glBindFramebuffer(GL_FRAMEBUFFER, shadow_buffer[0]);
		gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadow_tex[0], 0);
	
		gl.glDrawBuffer(GL_NONE);
		gl.glEnable(GL_DEPTH_TEST);

		gl.glEnable(GL_POLYGON_OFFSET_FILL);	// for reducing
		gl.glPolygonOffset(2.0f, 4.0f);			//  shadow artifacts
		
		offCurrentLight.setAmbient(amb4);
		offCurrentLight.setDiffuse(dif4);
		offCurrentLight.setSpecular(spec4);
		offCurrentLight.setConstantAtt(0.0f);
		offCurrentLight.setLinearAtt(0.0f);
		offCurrentLight.setQuadraticAtt(0.0f);
		
		if(lightSwitchListener.lightMode() == 0)// turn the lights off
		{
			offCurrentLight.setPosition(currentLight.getPosition());
			currentLight = offCurrentLight;
		}
		else	// else turn the lights back on
		{
			currentLight = onCurrentLight;
		}
		
		passOne();

		
		gl.glDisable(GL_POLYGON_OFFSET_FILL);	// artifact reduction, continued
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glActiveTexture(GL_TEXTURE0);	// shadow texture sampler
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
	
		gl.glDrawBuffer(GL_FRONT);
		
		passTwo();
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passOne()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program1);
		
		Point3D origin = new Point3D(0.0, 0.0, 0.0);
		Vector3D up = new Vector3D(0.0, 1.0, 0.0);
		lightV_matrix.setToIdentity();
		lightP_matrix.setToIdentity();
	
		lightV_matrix = lookAt(currentLight.getPosition(), origin, up);	// vector from light to origin
		lightP_matrix = perspective(50.0f, aspect, 0.1f, 1000.0f);

		///////////////////////////////////////
		/////////	 DRAW TORUS		///////////
		///////////////////////////////////////
		
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		int shadow_location = gl.glGetUniformLocation(rendering_program1, "shadowMVP");
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);	
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);

		//////////////////////////////////////////
		//////////	 DRAW PYRAMID	//////////////
		//////////////////////////////////////////
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);

		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		// set up pyramid vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
		
		
		//////////////////////////////////////////
		//////	 DRAW SPHERE LIGHT SOURCE	//////
		//////////////////////////////////////////
		
		m_matrix.setToIdentity();
		m_matrix.translate(sphereLoc.getX(), sphereLoc.getY(), sphereLoc.getZ());
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		//set up vertices buffer for light source sphere
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
		
		//////////////////////////////////////////
		//////	 DRAW SPHERE 3RD OBJECT		//////
		//////////////////////////////////////////
		
		m_matrix.setToIdentity();
		m_matrix.translate(sphereObjLoc.getX(), sphereObjLoc.getY(), sphereObjLoc.getZ());
		
		shadowMVP1.setToIdentity();
		shadowMVP1.concatenate(lightP_matrix);
		shadowMVP1.concatenate(lightV_matrix);
		shadowMVP1.concatenate(m_matrix);
		
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP1.getFloatValues(), 0);
		
		//set up vertices buffer for the 3rd object that is a sphere
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
		
		
		/////////////////////////////////////////////////
		////////		DRAW AXIS 		/////////////////
		/////////////////////////////////////////////////
		
		if(spaceListener.axesMode() == 1)
		{
			
			//  X - AXIS
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			// set up vertices for the x axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			// Y - AXIS
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			// set up vertices for the y axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			// Z - AXIS
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			// set up vertices for the z axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0 , 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			
		}
				
		
		
				
	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public void passTwo()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		gl.glUseProgram(rendering_program2);

		///////////////////////////////////////////////
		////////////	CAMERA			///////////////
		///////////////////////////////////////////////
		
		// calls computeView from Camera class to compute the panning degree's of the camera
		//  build the VIEW matrix
		v_matrix.setToIdentity();
		panDegreeUpDown = Cam.computeView(panDegreeUpDown, 1);
		panDegreeRightLeft = Cam.computeView(panDegreeRightLeft, 2);
		v_matrix.rotateX(panDegreeUpDown);
		v_matrix.rotateY(panDegreeRightLeft);
		v_matrix.translate(-Cam.getCamX(),-Cam.getCamY(),-Cam.getCamZ());
		
		///////////////////////////////////////////////
		////////////	DRAW TORUS		///////////////
		///////////////////////////////////////////////
		
		// inputs the PERAL material into thisMaterial
		thisMaterial = new Material();	
		float[] amb10 = new float[] {0.25f, 0.20725f, 0.20725f, 0.922f};
		float[] dif10 = new float[] {1.00f, 0.829f, 0.829f, 0.922f};
		float[] spec10 = new float[] {0.2966f, 0.2966f, 0.2966f, 0.922f};
		thisMaterial.setAmbient(amb10);
		thisMaterial.setDiffuse(dif10);
		thisMaterial.setSpecular(spec10);
		thisMaterial.setShininess(11.264f);
		
		mv_location = gl.glGetUniformLocation(rendering_program2, "mv_matrix");
		proj_location = gl.glGetUniformLocation(rendering_program2, "proj_matrix");
		n_location = gl.glGetUniformLocation(rendering_program2, "normalMat");
		int shadow_location = gl.glGetUniformLocation(rendering_program2,  "shadowMVP");
		
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(torusLoc.getX(),torusLoc.getY(),torusLoc.getZ());
		m_matrix.rotateX(25.0);
		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		// set up torus vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up torus normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);	

		// set up torus texture buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		// TEXTURE FUNCTION ONLY
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
	
		gl.glDrawArrays(GL_TRIANGLES, 0, numTorusVertices);
		
		/////////////////////////////////////////////////
		////////		DRAW A PYRAMID	/////////////////
		/////////////////////////////////////////////////
		
		// inputs the JADE material
		thisMaterial = new Material();
		float[] amb0 = new float[] {0.135f, 0.2225f, 0.1575f, 0.95f};
		float[] dif0 = new float[] {0.54f, 0.89f, 0.63f, 0.95f};
		float[] spec0 = new float[] {0.3162f, 0.3162f, 0.3162f, 0.95f};
		thisMaterial.setAmbient(amb0);
		thisMaterial.setDiffuse(dif0);
		thisMaterial.setSpecular(spec0);
		thisMaterial.setShininess(12.8f);
		
		installLights(rendering_program2, v_matrix);
		
		//  build the MODEL matrix
		m_matrix.setToIdentity();
		m_matrix.translate(pyrLoc.getX(),pyrLoc.getY(),pyrLoc.getZ());
		m_matrix.rotateX(30.0);
		m_matrix.rotateY(40.0);

		//  build the MODEL-VIEW matrix
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// set up texture buffer for pyramid
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		// TEXTURE FUNCTION ONLY
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, brickTexture);
		
		

		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, pyramid.getNumVertices());
		
		/////////////////////////////////////////////////
		////////	DRAW A SPHERE AT LIGHT SOURCE	/////
		/////////////////////////////////////////////////
		
		// makes the material of the sphere more yellow'ish, like a sun
		thisMaterial = new Material();
		float[] amb3 = new float[] {1.0f, 1.0f, 0.6f, 1.0f};
		float[] dif3 = new float[] {1.0f, 1.0f, 0.6f, 1.0f};
		float[] spec3 = new float[] {1.0f, 1.0f, 0.6f, 1.0f};
		thisMaterial.setAmbient(amb3);
		thisMaterial.setDiffuse(dif3);
		thisMaterial.setSpecular(spec3);
		thisMaterial.setShininess(10.0f);
		installLights(rendering_program2, v_matrix);
		
		m_matrix.setToIdentity();
		
		sphereLoc.setX(lightSource.getLightX());
		sphereLoc.setY(lightSource.getLightY());
		sphereLoc.setZ(lightSource.getLightZ());
		m_matrix.scale(0.3, 0.3, 0.3);
		
		m_matrix.translate(sphereLoc.getX(), sphereLoc.getY(), sphereLoc.getZ());
		
		// building the MODEL-VIEW MATRIX for the sphere that will be placed at the light soruce
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);

		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up sphere vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		// set up sphere normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		


		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
		
		
		//////////////////////////////////////////
		//////	 DRAW SPHERE 3RD OBJECT		//////
		//////////////////////////////////////////
		
		// uses bronze material for the 3rd object that is a sphere
		thisMaterial = graphicslib3D.Material.BRONZE;
		installLights(rendering_program2, v_matrix);
		
		m_matrix.setToIdentity();
		m_matrix.translate(sphereObjLoc.getX(), sphereObjLoc.getY(), sphereObjLoc.getZ());
		
		mv_matrix.setToIdentity();
		mv_matrix.concatenate(v_matrix);
		mv_matrix.concatenate(m_matrix);
		
		shadowMVP2.setToIdentity();
		shadowMVP2.concatenate(b);
		shadowMVP2.concatenate(lightP_matrix);
		shadowMVP2.concatenate(lightV_matrix);
		shadowMVP2.concatenate(m_matrix);
		gl.glUniformMatrix4fv(shadow_location, 1, false, shadowMVP2.getFloatValues(), 0);
		
		//  put the MV and PROJ matrices into the corresponding uniforms
		gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
		gl.glUniformMatrix4fv(n_location, 1, false, (mv_matrix.inverse()).transpose().getFloatValues(), 0);
		
		// set up sphere vertices buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		// set up sphere normals buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		// set up sphere texture coordinates buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);
		
		//TEXTURE FUNCTION ONLY
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, iceTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glDrawArrays(GL_TRIANGLES, 0, mySphere.getIndices().length);
		
		
		
		/////////////////////////////////////////////////
		////////		DRAW AXIS 		/////////////////
		/////////////////////////////////////////////////
		
		if(spaceListener.axesMode() == 1)
		{
			
			installLights(rendering_program2, v_matrix);
			///////////////////////////////////////////////////
			///////////// X AXIS RED LINE  ////////////////////
			///////////////////////////////////////////////////
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			//build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
			gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);
			
			//TEXTURE FUNCTIONS ONLY
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, redTexture);
			
			gl.glEnable(GL_DEPTH_TEST);
			gl.glFrontFace(GL_CCW);
			//  END OF TEXTURE FUNCTINOS

			gl.glDrawArrays(GL_LINES, 0, 2);
			
			///////////////////////////////////////////////////////////
			////////////// Y AXIES GREEN LINE /////////////////////////
			///////////////////////////////////////////////////////////
			
		
			installLights(rendering_program2, v_matrix);
			
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			//build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			
			
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
			gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);
			
			//TEXTURE FUNCTIONS ONLY
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, greenTexture);
			
			gl.glEnable(GL_DEPTH_TEST);
			gl.glFrontFace(GL_CCW);
			//  END OF TEXTURE FUNCTINOS

			gl.glDrawArrays(GL_LINES, 0, 2);
			
			///////////////////////////////////////////////////////////////
			//////////////////// Z AXIS BLUE LINE /////////////////////////
			///////////////////////////////////////////////////////////////
			
		
			installLights(rendering_program2, v_matrix);
			
			m_matrix.setToIdentity();
			m_matrix.translate(axisLoc.getX(), axisLoc.getY(), axisLoc.getZ());
			
			//build the MODEL-VIEW matrix
			mv_matrix.setToIdentity();
			mv_matrix.concatenate(v_matrix);
			mv_matrix.concatenate(m_matrix);
			
			
			
			//  put the MV and PROJ matrices into the corresponding uniforms
			gl.glUniformMatrix4fv(mv_location, 1, false, mv_matrix.getFloatValues(), 0);
			gl.glUniformMatrix4fv(proj_location, 1, false, proj_matrix.getFloatValues(), 0);
			
			// set up vertices buffer
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
			gl.glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(2);
			
			//TEXTURE FUCNTIONSO ONLY
			gl.glActiveTexture(GL_TEXTURE1);
			gl.glBindTexture(GL_TEXTURE_2D, blueTexture);
			
			gl.glEnable(GL_DEPTH_TEST);
			gl.glFrontFace(GL_CCW);
			//  END OF TEXTURE FUNCTINOS

			gl.glDrawArrays(GL_LINES, 0, 2);
		}

	}
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


	
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		createShaderPrograms();
		setupVertices();
		setupShadowBuffers();
		
				
		b.setElementAt(0,0,0.5);b.setElementAt(0,1,0.0);b.setElementAt(0,2,0.0);b.setElementAt(0,3,0.5f);
		b.setElementAt(1,0,0.0);b.setElementAt(1,1,0.5);b.setElementAt(1,2,0.0);b.setElementAt(1,3,0.5f);
		b.setElementAt(2,0,0.0);b.setElementAt(2,1,0.0);b.setElementAt(2,2,0.5);b.setElementAt(2,3,0.5f);
		b.setElementAt(3,0,0.0);b.setElementAt(3,1,0.0);b.setElementAt(3,2,0.0);b.setElementAt(3,3,1.0f);
		
		// may reduce shadow border artifacts
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		
		// initilizing the texture variables
		joglIceTexture = loadTexture("ice.jpg");
		iceTexture = joglIceTexture.getTextureObject();
		
		joglBrickTexture = loadTexture("brick1.jpg");
		brickTexture = joglBrickTexture.getTextureObject();
		
		joglEarthTexture = loadTexture("earth.jpg");
		earthTexture = joglEarthTexture.getTextureObject();
		
		joglRedTexture = loadTexture("red.jpg");
		redTexture = joglRedTexture.getTextureObject();
		
		joglGreenTexture = loadTexture("green.jpg");
		greenTexture = joglGreenTexture.getTextureObject();
		
		joglBlueTexture = loadTexture("blue.jpg");
		blueTexture = joglBlueTexture.getTextureObject();
	
	}
	
	public void setupShadowBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		scSizeX = myCanvas.getWidth();
		scSizeY = myCanvas.getHeight();
	
		gl.glGenFramebuffers(1, shadow_buffer, 0);
	
		gl.glGenTextures(1, shadow_tex, 0);
		gl.glBindTexture(GL_TEXTURE_2D, shadow_tex[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32,
						scSizeX, scSizeY, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
	}

// -----------------------------
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		setupShadowBuffers();
	}

	private void setupVertices()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		/////////////////////////////////
		/////		PYRAMID 		/////
		/////////////////////////////////
		// Pyramid vertex, normal, and texture vertices production
	
		Vertex3D[] pyramid_vertices = pyramid.getVertices();
		numPyramidVertices = pyramid.getNumVertices();
		float[] pyramid_vertex_positions = new float[numPyramidVertices*3];
		float[] pyramid_normals = new float[numPyramidVertices*3];
		float[] pyramid_texture = new float[numPyramidVertices*2];
		

		for (int i=0; i<numPyramidVertices; i++)
		{	pyramid_vertex_positions[i*3]   = (float) (pyramid_vertices[i]).getX();			
			pyramid_vertex_positions[i*3+1] = (float) (pyramid_vertices[i]).getY();
			pyramid_vertex_positions[i*3+2] = (float) (pyramid_vertices[i]).getZ();
			pyramid_texture[i*2] = (float) (pyramid_vertices[i]).getS();
			pyramid_texture[i*2+1] = (float) (pyramid_vertices[i]).getT();
			pyramid_normals[i*3]   = (float) (pyramid_vertices[i]).getNormalX();
			pyramid_normals[i*3+1] = (float) (pyramid_vertices[i]).getNormalY();
			pyramid_normals[i*3+2] = (float) (pyramid_vertices[i]).getNormalZ();
		}

		
		//////////////////////////////////
		////	Torus		//////////////
		//////////////////////////////////
		// Torus vertex, normal, and texture vertices production
		
		Vertex3D[] torus_vertices = myTorus.getVertices();
		int[] torus_indices = myTorus.getIndices();	
		float[] torus_fvalues = new float[torus_indices.length*3];
		float[] torus_nvalues = new float[torus_indices.length*3];
		float[] torus_tvalues = new float[torus_indices.length*2];
		
		for (int i=0; i<torus_indices.length; i++)
		{	torus_fvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getX();			
			torus_fvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getY();
			torus_fvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getZ();
			torus_tvalues[i*2] = (float) (torus_vertices[torus_indices[i]]).getS();
			torus_tvalues[i*2+1] = (float) (torus_vertices[torus_indices[i]]).getT();
			torus_nvalues[i*3]   = (float) (torus_vertices[torus_indices[i]]).getNormalX();
			torus_nvalues[i*3+1] = (float) (torus_vertices[torus_indices[i]]).getNormalY();
			torus_nvalues[i*3+2] = (float) (torus_vertices[torus_indices[i]]).getNormalZ();
		}
		
		//////////////////////////////////
		////	SPHERE LIGHT SOURCE //////	
		//////////////////////////////////
		// Sphere vertex, normal, and texture vertices production
		
		
		Vertex3D[] vertices = mySphere.getVertices();
		int[] indices = mySphere.getIndices();
		
		float[] tvalues = new float[indices.length*2];
		float[] pvalues = new float[indices.length*3];
		float[] nvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++)
		{	pvalues[i*3] = (float) (vertices[indices[i]]).getX();
			pvalues[i*3+1] = (float) (vertices[indices[i]]).getY();
			pvalues[i*3+2] = (float) (vertices[indices[i]]).getZ();
			tvalues[i*2] = (float) (vertices[indices[i]]).getS();
			tvalues[i*2+1] = (float) (vertices[indices[i]]).getT();
			nvalues[i*3] = (float) (vertices[indices[i]]).getNormalX();
			nvalues[i*3+1]= (float)(vertices[indices[i]]).getNormalY();
			nvalues[i*3+2]=(float) (vertices[indices[i]]).getNormalZ();
		}

		////////////////////////////////////////
		/////	X, Y, Z AXIS COORDINATES ///////
		////////////////////////////////////////
		
		float[] lineZ_coordinates =
		{
			0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 20.0f
		};
		
		float[] lineX_coordinates = 
		{
			0.0f, 0.0f, 0.0f, 20.0f, 0.0f, 0.0f
		};
		
		float[] lineY_coordinates =
		{
			0.0f, 0.0f, 0.0f, 0.0f, 20.0f, 0.0f		
		};
		
		float[] line_texture =
		{
			0.0f, 0.0f, 1.0f, 0.0f	
		};
		
		
		numTorusVertices = torus_indices.length;

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		gl.glGenBuffers(vbo.length, vbo, 0);

		//  put the Torus vertices into the first buffer,
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(torus_fvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);
		
		//  load the pyramid vertices into the second buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		FloatBuffer pyrVertBuf = Buffers.newDirectFloatBuffer(pyramid_vertex_positions);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrVertBuf.limit()*4, pyrVertBuf, GL_STATIC_DRAW);
		
		// load the torus normal coordinates into the third buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer torusNorBuf = Buffers.newDirectFloatBuffer(torus_nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusNorBuf.limit()*4, torusNorBuf, GL_STATIC_DRAW);
		
		// load the pyramid normal coordinates into the fourth buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer pyrNorBuf = Buffers.newDirectFloatBuffer(pyramid_normals);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrNorBuf.limit()*4, pyrNorBuf, GL_STATIC_DRAW);
		
		// load the x axis vertices into the 4th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer xBuf = Buffers.newDirectFloatBuffer(lineX_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, xBuf.limit()*4, xBuf, GL_STATIC_DRAW);
		
		// loads the y axis vertices into the 5th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[5]);
		FloatBuffer yBuf = Buffers.newDirectFloatBuffer(lineY_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, yBuf.limit()*4, yBuf, GL_STATIC_DRAW);
		
		// loads the z axis vertices into the 6th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		FloatBuffer zBuf = Buffers.newDirectFloatBuffer(lineZ_coordinates);
		gl.glBufferData(GL_ARRAY_BUFFER, zBuf.limit()*4, zBuf, GL_STATIC_DRAW);
		
		// loads the sphere vertices into the 7th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		FloatBuffer sunPBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunPBuf.limit()*4, sunPBuf, GL_STATIC_DRAW);
		
		// loads the sphere normal vertices into the 8th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer sunNBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunNBuf.limit()*4, sunNBuf, GL_STATIC_DRAW);
		
		// loads the sphere texture vertices into the 9th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer sunTBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, sunTBuf.limit()*4, sunTBuf, GL_STATIC_DRAW);
		
		// loads the pyramid texture vertices into the 10th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer pyrTexBuf = Buffers.newDirectFloatBuffer(pyramid_texture);
		gl.glBufferData(GL_ARRAY_BUFFER, pyrTexBuf.limit()*4, pyrTexBuf, GL_STATIC_DRAW);
		
		// loads the torus texture vertices into the 11th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer torusTexBuf = Buffers.newDirectFloatBuffer(torus_tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, torusTexBuf.limit()*4, torusTexBuf, GL_STATIC_DRAW);
		
		// loads the axis texture vertices into the 12th buffer
		gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[12]);
		FloatBuffer axisTexBuf = Buffers.newDirectFloatBuffer(line_texture);
		gl.glBufferData(GL_ARRAY_BUFFER, axisTexBuf.limit()*4, axisTexBuf, GL_STATIC_DRAW);
		

	}
	
	private void installLights(int rendering_program, Matrix3D v_matrix)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		Material currentMaterial = new Material();
		currentMaterial = thisMaterial;
		
		Point3D lightP = currentLight.getPosition();
		Point3D lightPv = lightP.mult(v_matrix);
		
		float [] currLightPos = new float[] { (float) lightPv.getX(),
			(float) lightPv.getY(),
			(float) lightPv.getZ() };

		// get the location of the global ambient light field in the shader
		int globalAmbLoc = gl.glGetUniformLocation(rendering_program, "globalAmbient");
	
		// set the current globalAmbient settings
		gl.glProgramUniform4fv(rendering_program, globalAmbLoc, 1, globalAmbient, 0);

		// get the locations of the light and material fields in the shader
		int ambLoc = gl.glGetUniformLocation(rendering_program, "light.ambient");
		int diffLoc = gl.glGetUniformLocation(rendering_program, "light.diffuse");
		int specLoc = gl.glGetUniformLocation(rendering_program, "light.specular");
		int posLoc = gl.glGetUniformLocation(rendering_program, "light.position");

		int MambLoc = gl.glGetUniformLocation(rendering_program, "material.ambient");
		int MdiffLoc = gl.glGetUniformLocation(rendering_program, "material.diffuse");
		int MspecLoc = gl.glGetUniformLocation(rendering_program, "material.specular");
		int MshiLoc = gl.glGetUniformLocation(rendering_program, "material.shininess");

		// set the uniform light and material values in the shader
		gl.glProgramUniform4fv(rendering_program, ambLoc, 1, currentLight.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, diffLoc, 1, currentLight.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, specLoc, 1, currentLight.getSpecular(), 0);
		gl.glProgramUniform3fv(rendering_program, posLoc, 1, currLightPos, 0);
	
		gl.glProgramUniform4fv(rendering_program, MambLoc, 1, currentMaterial.getAmbient(), 0);
		gl.glProgramUniform4fv(rendering_program, MdiffLoc, 1, currentMaterial.getDiffuse(), 0);
		gl.glProgramUniform4fv(rendering_program, MspecLoc, 1, currentMaterial.getSpecular(), 0);
		gl.glProgramUniform1f(rendering_program, MshiLoc, currentMaterial.getShininess());
	}

	public static void main(String[] args) { new Starter(); }

	@Override
	public void dispose(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) drawable.getGL();
		gl.glDeleteVertexArrays(1, vao, 0);
	}

//-----------------
	private void createShaderPrograms()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] vertCompiled = new int[1];
		int[] fragCompiled = new int[1];
		int[] linked = new int[1];
	

		vBlinn1ShaderSource = util.readShaderSource("blinnVert1.shader");
		vBlinn2ShaderSource = util.readShaderSource("blinnVert2.shader");
		fBlinn2ShaderSource = util.readShaderSource("blinnFrag2.shader");
		

		int vertexShader1 = gl.glCreateShader(GL_VERTEX_SHADER);
		int vertexShader2 = gl.glCreateShader(GL_VERTEX_SHADER);
		int fragmentShader2 = gl.glCreateShader(GL_FRAGMENT_SHADER);
		


		gl.glShaderSource(vertexShader1, vBlinn1ShaderSource.length, vBlinn1ShaderSource, null, 0);
		gl.glShaderSource(vertexShader2, vBlinn2ShaderSource.length, vBlinn2ShaderSource, null, 0);
		gl.glShaderSource(fragmentShader2, fBlinn2ShaderSource.length, fBlinn2ShaderSource, null, 0);


		
		
		gl.glCompileShader(vertexShader1);
		gl.glCompileShader(vertexShader2);
		gl.glCompileShader(fragmentShader2);

		
		//		VERTEX SHADER ERROR CHECKER	
		shaderChecker.vShaderErrorChecker(vertexShader1, gl, vertCompiled);
		shaderChecker.vShaderErrorChecker(vertexShader2, gl, vertCompiled);

		
		// FRAG SHADER ERROR CHECKER
		shaderChecker.fShaderErrorChecker(fragmentShader2, gl, fragCompiled);


		rendering_program1 = gl.glCreateProgram();
		rendering_program2 = gl.glCreateProgram();


		gl.glAttachShader(rendering_program1, vertexShader1);
		
		gl.glAttachShader(rendering_program2, vertexShader2);
		gl.glAttachShader(rendering_program2, fragmentShader2);
		


		gl.glLinkProgram(rendering_program1);
		gl.glLinkProgram(rendering_program2);

		// link error checking
		shaderChecker.linkErrorChecker(rendering_program1, gl, linked);
		shaderChecker.linkErrorChecker(rendering_program2, gl, linked);

	}

//------------------
	private Matrix3D perspective(float fovy, float aspect, float n, float f)
	{	float q = 1.0f / ((float) Math.tan(Math.toRadians(0.5f * fovy)));
		float A = q / aspect;
		float B = (n + f) / (n - f);
		float C = (2.0f * n * f) / (n - f);
		Matrix3D r = new Matrix3D();
		r.setElementAt(0,0,A);
		r.setElementAt(1,1,q);
		r.setElementAt(2,2,B);
		r.setElementAt(3,2,-1.0f);
		r.setElementAt(2,3,C);
		r.setElementAt(3,3,0.0f);
		return r;
	}

	private Matrix3D lookAt(Point3D eye, Point3D target, Vector3D y)
	{	Vector3D eyeV = new Vector3D(eye);
		Vector3D targetV = new Vector3D(target);
		Vector3D fwd = (targetV.minus(eyeV)).normalize();
		Vector3D side = (fwd.cross(y)).normalize();
		Vector3D up = (side.cross(fwd)).normalize();
		Matrix3D look = new Matrix3D();
		look.setElementAt(0,0, side.getX());
		look.setElementAt(1,0, up.getX());
		look.setElementAt(2,0, -fwd.getX());
		look.setElementAt(3,0, 0.0f);
		look.setElementAt(0,1, side.getY());
		look.setElementAt(1,1, up.getY());
		look.setElementAt(2,1, -fwd.getY());
		look.setElementAt(3,1, 0.0f);
		look.setElementAt(0,2, side.getZ());
		look.setElementAt(1,2, up.getZ());
		look.setElementAt(2,2, -fwd.getZ());
		look.setElementAt(3,2, 0.0f);
		look.setElementAt(0,3, side.dot(eyeV.mult(-1)));
		look.setElementAt(1,3, up.dot(eyeV.mult(-1)));
		look.setElementAt(2,3, (fwd.mult(-1)).dot(eyeV.mult(-1)));
		look.setElementAt(3,3, 1.0f);
		return(look);
	}

	// function to read in the texture jpg file
	public Texture loadTexture(String textureFileName)
	{	Texture tex = null;
		try { tex = TextureIO.newTexture(new File(textureFileName), false); }
		catch (Exception e) { e.printStackTrace(); }
		return tex;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}