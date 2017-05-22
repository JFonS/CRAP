package CRAP;

import org.lwjgl.*;
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

public class Camera extends Data
{
	static float rotationX = 0.0f;
	static float rotationY = 0.0f;
	static float zoom = -5.0f;
	
	public Camera()
	{
	}
	
	public void LoadProjection()
	{
		Camera.perspectiveGL(60.0f, 1.0f, 0.1f, 1000.0f);
	}
	
	public void LoadView()
	{
		glTranslatef(0.0f, 0.0f, zoom);
		glRotatef(rotationX, 1.0f, 0.0f, 0.0f);
		glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
	}
	
	public void Update()
	{
		float rotSpeed = 1.0f;
		if ( glfwGetKey(Main.window, GLFW_KEY_W) > 0 )
		{
			rotationX += rotSpeed;
		}
		else if ( glfwGetKey(Main.window, GLFW_KEY_S) > 0 )
		{
			rotationX -= rotSpeed;
		}
		
		if ( glfwGetKey(Main.window, GLFW_KEY_A) > 0 )
		{
			rotationY += rotSpeed;
		}
		else if ( glfwGetKey(Main.window, GLFW_KEY_D) > 0 )
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
