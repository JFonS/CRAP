package CRAP;


import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;

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
		glPushMatrix();
		ApplyObjectTransform(object);
		ARBShaderObjects.glUseProgramObjectARB(Main.programID);
			
		switch ( object.getProperty("Primitive").getStringValue() )
		{
			case "Cube":
				DrawCube(1.0f, color);
			break;

			case "Sphere":
				DrawSphere(1.0, 10, 10, color);
			break;
			
			case "Cylinder":
				DrawCylinder(1.0f, 1.0f, color);
			break;
		}
		
		glEnd();
		glPopMatrix();
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
		GL20.glUniform4f(GL20.glGetUniformLocation(Main.programID, "c"), color.Get(0), color.Get(1), color.Get(2), 1.0f);
		
		glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, 
			     new float[]{color.Get(0) * 0.5f, color.Get(1) * 0.5f, color.Get(2) * 0.5f, 1});
		glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, 
				     new float[]{color.Get(0), color.Get(1), color.Get(2), 1});
		glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, new float[]{color.Get(0) * 0.5f, color.Get(1) * 0.5f, color.Get(2) * 0.5f, 1});
		glMaterialfv(GL_FRONT_AND_BACK, GL_SHININESS, new float[]{color.Get(0), color.Get(1), color.Get(2), 1});
		
		glColor4f(color.Get(0), color.Get(1), color.Get(2), 1.0f);
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

			SetMaterial(color);
			
			glBegin(GL_QUAD_STRIP);
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
	
	private static void DrawCylinder(float radius, float height, Vec color)
	{
		float x              = 0.0f;
		float y              = 0.0f;
		float angle          = 0.0f;
		float angle_stepsize = 0.1f;
		
		SetMaterial(color);
		
		/** Draw the tube */
		glBegin(GL_QUAD_STRIP);
		angle = 0.0f;
		 while( angle < 2*Math.PI) {
		     x = (float) (radius * Math.cos(angle));
		     y = (float) (radius * Math.sin(angle));
		     glNormal3f((float)Math.cos(angle),
		    		    (float)Math.sin(angle), 0.0f);
		     glVertex3f(x, y, height);
		     glVertex3f(x, y, 0.0f);
		     angle = angle + angle_stepsize;
		 }
		 glVertex3f(radius, 0.0f, height);
		 glVertex3f(radius, 0.0f, 0.0f);
		glEnd();
		
		/** Draw the circle on top of cylinder */
		glBegin(GL_POLYGON);
		SetMaterial(color);
		angle = 0.0f;
		while( angle < 2*Math.PI ) {
		     x = (float) (radius * Math.cos(angle));
		     y = (float) (radius * Math.sin(angle));
		     glNormal3f(0.0f, 0.0f, 1.0f);
		     glVertex3f(x, y , height);
		     angle = angle + angle_stepsize;
		}
		glVertex3f(radius, 0.0f, height);
		glEnd();

		/** Draw the circle on bot of cylinder */
		glBegin(GL_POLYGON);
		angle = 0.0f;
		while( angle < 2*Math.PI ) {
		     x = (float) (radius * Math.cos(angle));
		     y = (float) (radius * Math.sin(angle));
		     glNormal3f(0.0f, 0.0f, -1.0f);
		     glVertex3f(x, y, 0.0f);
		     angle = angle + angle_stepsize;
		}
		glVertex3f(radius, 0.0f, height);
		glEnd();
	}
	
	private static void DrawCube(float side, Vec color)
	{
	   SetMaterial(color);
	   glBegin(GL_POLYGON);
		 glNormal3f(0.0f, 0.0f, 1.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(-side,side,side);
	     glVertex3f(-side,-side,side);
	     glVertex3f(side,-side,side);
	   glEnd();

	   glBegin(GL_POLYGON);
		 glNormal3f(1.0f, 0.0f, 0.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(side,-side,side);
	     glVertex3f(side,-side,-side);
	     glVertex3f(side,side,-side);
	   glEnd();
	   
	   glBegin(GL_POLYGON);
		glNormal3f(0.0f, 0.0f, -1.0f);
	     glVertex3f(side,side,-side);
	     glVertex3f(side,-side,-side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(-side,side,-side);
	   glEnd();

	   glBegin(GL_POLYGON);
		 glNormal3f(-1.0f, 0.0f, 0.0f);
	     glVertex3f(-side,side,side);
	     glVertex3f(-side,side,-side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(-side,-side,side);
	   glEnd();

	   glBegin(GL_POLYGON);
		 glNormal3f(0.0f, 1.0f, 0.0f);
	     glVertex3f(side,side,side);
	     glVertex3f(side,side,-side);
	     glVertex3f(-side,side,-side);
	     glVertex3f(-side,side,side);
	   glEnd();
	   
	   glBegin(GL_POLYGON);
		 glNormal3f(0.0f, -1.0f, 0.0f);
	     glVertex3f(side,-side,side);
	     glVertex3f(-side,-side,side);
	     glVertex3f(-side,-side,-side);
	     glVertex3f(side,-side,-side);
	glEnd();
	}
}
