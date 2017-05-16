package CRAP;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;

import interp.Data;

public class ObjectRenderer {
	
	public static void ApplyObjectTransform(Data object)
	{
		Data parent = object.getProperty("Parent");
		if (parent != null && parent != object)
		{
			ApplyObjectTransform(parent);
		}
		
		Vec pos   = object.getProperty("Position").getVecValue();
		Vec rot   = object.getProperty("Rotation").getVecValue();
		Vec scale = object.getProperty("Scale").getVecValue();
		
		glTranslatef(pos.Get("x"), pos.Get("y"), pos.Get("z"));
		glRotatef(rot.Get("x"), 1, 0, 0);
		glRotatef(rot.Get("y"), 0, 1, 0);
		glRotatef(rot.Get("z"), 0, 0, 1);
		glScalef(scale.Get("x"), scale.Get("y"), scale.Get("z"));
	}
	
	public static void RenderObject(Data object) 
	{
		//long time = System.nanoTime();
		if ( !IsVisible(object) ) { return; }

		Vec color = object.getProperty("Color").getVecValue();
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		ApplyObjectTransform(object);
		
		
		switch ( object.getProperty("Primitive").getStringValue() )
		{
			case "Cube":
				DrawCube(1.0f, color);
			break;

			case "Sphere":
				DrawSphere(1.0, 10, 10, color);
			break;
		}
		
		glEnd();
		//System.out.println( object + ": " + (System.nanoTime() - time) ); time = System.nanoTime();
	}

	public static boolean IsVisible(Data obj)
	{
		if (obj == null) { return true; }
		Data visible = obj.getProperty("Visible");
		return visible != null && visible.getBooleanValue() && 
			   IsVisible(obj.getProperty("Parent"));
	}
	
	private static void SetMaterial(Vec color)
	{
		glMaterialfv(GL_FRONT, GL_DIFFUSE, 
				     new float[]{color.Get(0), color.Get(1), color.Get(2), 1});
		System.out.println(color);
	}
	private static void DrawSphere(double r, int lats, int longs, Vec color) {
		int i, j;

		for (i = 0; i <= lats; i++) {
			double lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
			double z0 = Math.sin(lat0);
			double zr0 = Math.cos(lat0);

			double lat1 = Math.PI * (-0.5 + (double) i / lats);
			double z1 = Math.sin(lat1);
			double zr1 = Math.cos(lat1);

			glBegin(GL_QUAD_STRIP);
			SetMaterial(color);
			for (j = 0; j <= longs; j++) {
				double lng = 2 * Math.PI * (double) (j - 1) / longs;
				double x = Math.cos(lng);
				double y = Math.sin(lng);

				glNormal3d(x * zr0, y * zr0, z0);
				glVertex3d(x * zr0, y * zr0, z0);
				
				glNormal3d(x * zr1, y * zr1, z1);
				glVertex3d(x * zr1, y * zr1, z1);
			}
			glEnd();
		}
	}
	
	private static void DrawCube(float side, Vec color)
	{
	   glBegin(GL_POLYGON);
		SetMaterial(color);
		 glNormal3f(0.0f, 0.0f, 1.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(-side,side,side);
	     glVertex3f(-side,-side,side);
	     glVertex3f(side,-side,side);
	   glEnd();

	   glBegin(GL_POLYGON);
		SetMaterial(color);
		 glNormal3f(1.0f, 0.0f, 0.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(side,-side,side);
	     glVertex3f(side,-side,-side);
	     glVertex3f(side,side,-side);
	   glEnd();
	   
	   glBegin(GL_POLYGON);
		SetMaterial(color);
		glNormal3f(0.0f, 0.0f, -1.0f);
	     glVertex3f(side,side,-side);
	     glVertex3f(side,-side,-side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(-side,side,-side);
	   glEnd();

	   glBegin(GL_POLYGON);
		SetMaterial(color);
		 glNormal3f(-1.0f, 0.0f, 0.0f);
	     glVertex3f(-side,side,side);
	     glVertex3f(-side,side,-side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(-side,-side,side);
	   glEnd();

	   glBegin(GL_POLYGON);
		SetMaterial(color);
		 glNormal3f(0.0f, 1.0f, 0.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(side,side,-side);
	     glVertex3f(-side,side,-side);
	     glVertex3f(-side,side,side);
	   glEnd();
	   
	   glBegin(GL_POLYGON);
		SetMaterial(color);
		 glNormal3f(0.0f, -1.0f, 0.0f);
	     glVertex3f(side,-side,side);
	     glVertex3f(-side,-side,side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(side,-side,-side);
	glEnd();
	}
}
