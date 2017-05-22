package CRAP;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import interp.Data;
import interp.Interp;

import java.nio.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
	public static long window;

	public static void main(String[] args) throws Exception {
		CRAP.Init(args);
		Init();

		if (CRAP.execute) {
			Loop();

			glfwFreeCallbacks(window);
			glfwDestroyWindow(window);
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}
	}

	private static void Init() {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints(); // optional, the current window hints are
									// already the default
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		// glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden
													// after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be
													// resizable

		// Create the window
		window = glfwCreateWindow(600, 600, "Hello World!", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed,
		// repeated or released.
		/*
		 * glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
		 * if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
		 * glfwSetWindowShouldClose(window, true); // We will detect this in the
		 * rendering loop });
		 */

		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			glfwGetWindowSize(window, pWidth, pHeight);
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2,
					(vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		Time.Init();
	}

	private static void Loop() {
		GL.createCapabilities();

		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		Camera.GetInstance().LoadProjection();

		glEnable(GL_LIGHTING);
		glEnable(GL_NORMALIZE);
		glEnable(GL_DEPTH_TEST);
		// glEnable(GL_COLOR_MATERIAL);
		glShadeModel(GL_SMOOTH);

		glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			Camera.GetInstance().Update();
			Update();
		}
		glPopMatrix();
	}

	private static void Update() {
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		Camera.GetInstance().LoadView();
		Main.ApplyLights();

		HashSet<Data> aliveDatas = GetAliveDatas();
		for (Data data : aliveDatas) {
			if (!data.isObject()) {
				continue;
			}
			ObjectRenderer.RenderObject(data);
		}

		Time.Update();
		CRAP.interp.Update();

		glfwSwapBuffers(window); // swap the color buffers
		glfwPollEvents();
	}

	public static void ApplyLights() {
		Data light0 = CRAP.interp.stack.getVariable("Light0");
		Data light1 = CRAP.interp.stack.getVariable("Light1");
		Data light2 = CRAP.interp.stack.getVariable("Light2");

		if (light0.getProperty("Visible").getBooleanValue())
			glEnable(GL_LIGHT0);
		else
			glDisable(GL_LIGHT0);
		if (light1.getProperty("Visible").getBooleanValue())
			glEnable(GL_LIGHT1);
		else
			glDisable(GL_LIGHT1);
		if (light2.getProperty("Visible").getBooleanValue())
			glEnable(GL_LIGHT2);
		else
			glDisable(GL_LIGHT2);

		float light0Pos_3[] = light0.getProperty("Position").getVecValue()
				.GetValues();
		float light0Col_3[] = light0.getProperty("Color").getVecValue()
				.GetValues();
		float light0Pos_4[] = { light0Pos_3[0], light0Pos_3[1], light0Pos_3[2],
				1 };
		float light0Col_4[] = { light0Col_3[0], light0Col_3[1], light0Col_3[2],
				1 };

		float light1Pos_3[] = light1.getProperty("Position").getVecValue()
				.GetValues();
		float light1Col_3[] = light1.getProperty("Color").getVecValue()
				.GetValues();
		float light1Pos_4[] = { light1Pos_3[0], light1Pos_3[1], light1Pos_3[2],
				1 };
		float light1Col_4[] = { light1Col_3[0], light1Col_3[1], light1Col_3[2],
				1 };

		float light2Pos_3[] = light2.getProperty("Position").getVecValue()
				.GetValues();
		float light2Col_3[] = light2.getProperty("Color").getVecValue()
				.GetValues();
		float light2Pos_4[] = { light2Pos_3[0], light2Pos_3[1], light2Pos_3[2],
				1 };
		float light2Col_4[] = { light2Col_3[0], light2Col_3[1], light2Col_3[2],
				1 };

		glLightfv(GL_LIGHT0, GL_POSITION, light0Pos_4);
		glLightfv(GL_LIGHT0, GL_DIFFUSE, light0Col_4);
		glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 1.0f);
		glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION,
				1.0f / light0.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 1.0f / light0
				.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT0, GL_SPOT_EXPONENT, light0.getProperty("Intensity")
				.getNumberValue());

		glLightfv(GL_LIGHT1, GL_POSITION, light1Pos_4);
		glLightfv(GL_LIGHT1, GL_DIFFUSE, light1Col_4);
		glLightf(GL_LIGHT1, GL_CONSTANT_ATTENUATION, 1.0f);
		glLightf(GL_LIGHT1, GL_LINEAR_ATTENUATION,
				1.0f / light1.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT1, GL_QUADRATIC_ATTENUATION, 1.0f / light1
				.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT1, GL_SPOT_EXPONENT, light1.getProperty("Intensity")
				.getNumberValue());

		glLightfv(GL_LIGHT2, GL_POSITION, light2Pos_4);
		glLightfv(GL_LIGHT2, GL_DIFFUSE, light2Col_4);
		glLightf(GL_LIGHT2, GL_CONSTANT_ATTENUATION, 0.01f);
		glLightf(GL_LIGHT2, GL_LINEAR_ATTENUATION,
				1.0f / light2.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT2, GL_QUADRATIC_ATTENUATION, 1.0f / light2
				.getProperty("Range").getNumberValue());
		glLightf(GL_LIGHT2, GL_SPOT_EXPONENT, light2.getProperty("Intensity")
				.getNumberValue());
	}

	private static HashSet<Data> GetAliveDatas() {
		HashSet<Data> aliveDatas = new HashSet<Data>();
		aliveDatas.addAll(CRAP.interp.timelineManager.GetAliveDatas());
		aliveDatas.addAll(CRAP.interp.stack.GetGlobalDatas());

		ArrayList<Data> addedAliveDatas = new ArrayList<Data>();
		do // Please kill me
		{
			addedAliveDatas = new ArrayList<Data>();
			for (Data data : aliveDatas) {
				for (Data d : data.getChildren()) {
					if (!aliveDatas.contains(d)) {
						addedAliveDatas.add(d);
					}
				}
			}

			aliveDatas.addAll(addedAliveDatas);
		} while (addedAliveDatas.size() > 0);

		return aliveDatas;
	}

	private static void PrintAliveDatas() {
		HashSet<Data> aliveDatas = GetAliveDatas();
		String datas = "[";
		for (Data aliveData : aliveDatas) {
			if (aliveData.isObject()) {
				datas += aliveData.toString() + ", ";
			}
		}
		datas += "]";
		System.out.println(datas);
	}
}
