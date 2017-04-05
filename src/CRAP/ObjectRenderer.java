package CRAP;

import static org.lwjgl.opengl.GL11.*;
import interp.Data;

public class ObjectRenderer 
{
	public static void RenderObject(Data object)
	{
		float posx   = object.getProperty("Position.x").getNumberValue();
		float posy   = object.getProperty("Position.y").getNumberValue();
		float posz   = object.getProperty("Position.z").getNumberValue();
		float rotx   = object.getProperty("Rotation.x").getNumberValue();
		float roty   = object.getProperty("Rotation.y").getNumberValue();
		float rotz   = object.getProperty("Rotation.z").getNumberValue();
		float scalex = object.getProperty("Scale.x").getNumberValue();
		float scaley = object.getProperty("Scale.y").getNumberValue();
		float scalez = object.getProperty("Scale.z").getNumberValue();
		System.out.println(posx + "," + posy + "," + posz);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glTranslatef(posx, posy, posz);
		glRotatef(rotx, 1, 0, 0);
		glRotatef(roty, 0, 1, 0);
		glRotatef(rotz, 0, 0, 1);
		glScalef(scalex, scaley, scalez);
		
		glBegin(GL_QUADS);
	    glColor3f(0.0f, 0.0f, 1.0f); //green
	    glVertex3f(-1, -1, 0);
	    glVertex3f( 1, -1, 0);
	    glVertex3f( 1,  1, 0);
	    glVertex3f(-1,  1, 0);
	    glEnd();
	}
}
