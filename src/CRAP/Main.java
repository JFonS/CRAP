package CRAP;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import interp.Data;

import java.nio.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main 
{
    private static long window;
    
	public static void main(String[] args) throws Exception
	{
		CRAP.Init(args);
		Init();
		
		if (CRAP.execute) 
		{
	        Loop();

	        glfwFreeCallbacks(window); glfwDestroyWindow(window);
	        glfwTerminate(); glfwSetErrorCallback(null).free();
		}
	}

    private static void Init() 
    {
        GLFWErrorCallback.createPrint(System.err).set();

        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        //glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(600, 600, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        /*
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        */

        try ( MemoryStack stack = stackPush() ) 
        {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
        
        Time.Init();
    }

    private static void Loop() 
    {
        GL.createCapabilities();
        
        glClearColor(0.0f,1.0f, 0.0f, 0.0f);
        while ( !glfwWindowShouldClose(window) ) 
        {
        	Update();
        }
    }
    
    private static void perspectiveGL(float fovY, float aspect, float zNear, float zFar )
    {
        float pi = 3.141592f;
        float fW, fH;

        fH = (float) Math.tan( fovY / 360 * pi ) * zNear;
        fW = fH * aspect;

        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }
    
    private static void Update()
    {
    	glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        
        // Camera
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		perspectiveGL(60.0f, 1.0f, 0.1f, 1000.0f);

		glEnable(GL_LIGHTING);
		glEnable(GL_LIGHT0);
		glShadeModel (GL_SMOOTH);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glLightfv(GL_LIGHT0, GL_POSITION, new float[]{0,0,-8,1});
		glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[]{1,1,1,1});
		
    	HashSet<Data> aliveDatas = GetAliveDatas();
    	for (Data data : aliveDatas)
    	{
    		if (!data.isObject()) { continue; }
    		ObjectRenderer.RenderObject(data);
    	}
    	
        Time.Update();
        CRAP.interp.Update();
		
        glfwSwapBuffers(window); // swap the color buffers
        glfwPollEvents();
    }
    

    private static HashSet<Data> GetAliveDatas()
    {
    	HashSet<Data> aliveDatas = new HashSet<Data>();
    	aliveDatas.addAll( CRAP.interp.timelineManager.GetAliveDatas() );
    	aliveDatas.addAll( CRAP.interp.stack.GetGlobalDatas() );
    	
		ArrayList<Data> addedAliveDatas = new ArrayList<Data>();
    	do  // Please kill me
    	{
    		addedAliveDatas = new ArrayList<Data>();
    		for (Data data : aliveDatas)
	    	{
    			for (Data d : data.getChildren()) {
    				if (!aliveDatas.contains(d)) {
    					addedAliveDatas.add(d);
    				}
    			}
	    	}
    		
    		aliveDatas.addAll(addedAliveDatas);
    	}
    	while (addedAliveDatas.size() > 0);
    	
    	return aliveDatas;
    }
    
    private static void PrintAliveDatas()
    {
    	HashSet<Data> aliveDatas = GetAliveDatas();
    	String datas = "[";
    	for (Data aliveData : aliveDatas)
    	{
    		if (aliveData.isObject()) 
    		{ 
    			datas += aliveData.toString() + ", "; 
    		}
    	}
    	datas += "]";
    	System.out.println(datas);
    }
}
