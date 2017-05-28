package CRAP;

import org.lwjgl.*;

import static org.lwjgl.system.MemoryUtil.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.lwjgl.glfw.GLFWScrollCallback;

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

public class Camera
{
	static float rotationX = 0.0f;
	static float rotationY = 0.0f;
	static float zoom = -5.0f;
	static boolean mouseLocked = false;
	
	static double newX = 0;
    static double newY = 0;

    static double prevX = 0;
    static double prevY = 0;
    static int width, height;
	
	public Camera()
	{
	}
	
	public void LoadProjection(int screenWidth, int screenHeight)
	{
		glViewport(0, 0, screenWidth, screenHeight);
		Camera.perspectiveGL(60.0f, ((float)screenWidth)/screenHeight, 0.1f, 1000.0f);
	}
	
	public void LoadView()
	{
		glTranslatef(0.0f, 0.0f, zoom);
		glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
		glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
	}
	
	public void Update()
	{
		IntBuffer pWidth = memAllocInt(1); // int*
		IntBuffer pHeight = memAllocInt(1); // int*
		
		glfwGetWindowSize(Main.window, pWidth, pHeight);
		
		width = pWidth.get(0);
		height = pHeight.get(0);
		
		float rotSpeed = 0.3f;
		if (glfwGetMouseButton(Main.window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS) {
            glfwSetCursorPos(Main.window, width/2, height/2);
            glfwSetInputMode(Main.window,GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            mouseLocked = true;
        }

        if (mouseLocked){
            DoubleBuffer x = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer y = BufferUtils.createDoubleBuffer(1);

            glfwGetCursorPos(Main.window, x, y);
            x.rewind();
            y.rewind();

            newX = x.get();
            newY = y.get();

            double deltaX = newX - width/2;
            double deltaY = newY - height/2;

            if(newX != prevX) {
            	rotationY += rotSpeed*deltaX;

            }
            if( newY != prevY) {
            	rotationX += rotSpeed*deltaY;
            }

            prevX = newX;
            prevY = newY;

            glfwSetCursorPos(Main.window, width/2, height/2);
            
            if (glfwGetKey(Main.window, GLFW_KEY_ESCAPE) == 1)
            {
            	glfwSetInputMode(Main.window,GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            	mouseLocked = false;
            }
        }
        
		if ( glfwGetKey(Main.window, GLFW_KEY_W) == 1)
		{
			rotationX += rotSpeed;
		}
		if ( glfwGetKey(Main.window, GLFW_KEY_S) == 1)
		{
			rotationX -= rotSpeed;
		}
		
		if ( glfwGetKey(Main.window, GLFW_KEY_A) == 1)
		{
			rotationY += rotSpeed;
		}
		if ( glfwGetKey(Main.window, GLFW_KEY_D) == 1)
		{
			rotationY -= rotSpeed;
		}
		
        
        
		glfwSetScrollCallback(Main.window, new GLFWScrollCallbackI() {
		    @Override
		    public void invoke(long window, double xoffset, double yoffset) {
				zoom += yoffset * 1.0f;
		    }
		});
		
	}

    private static void perspectiveGL(float fovY, float aspect, float zNear, float zFar )
    {
        float pi = 3.141592f;
        float fW, fH;

        fH = (float) Math.tan( fovY / 360 * pi ) * zNear;
        fW = fH * aspect;

        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }
    
	public static Camera GetInstance()
	{
		if (camera == null)
		{
			camera = new Camera();
		}
		return camera;
	}
	
	private static Camera camera = null;
}
