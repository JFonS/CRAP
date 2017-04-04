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

    private static void Update()
    {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

    	HashSet<Data> aliveDatas = GetAliveDatas();
    	for (Data data : aliveDatas)
    	{
    		if (!data.isObject()) { continue; }
    		
    		float x = data.getProperty("Position.x").getNumberValue();
    		float y = data.getProperty("Position.y").getNumberValue();
    		
	        glBegin(GL_QUADS);
	        glColor3f(1.0f, 0.0f, 0.0f);
	        glVertex3d(x + 0.0f, y + 0.0f, 0.0f);
	        glVertex3d(x + 0.1f, y + 0.0f, 0.0f);
	        glVertex3d(x + 0.1f, y + 0.1f, 0.0f);
	        glVertex3d(x + 0.0f, y + 0.1f, 0.0f);
	        glEnd();
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