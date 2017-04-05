package CRAP;

import static org.lwjgl.opengl.GL11.*;
import interp.Data;

public class ObjectRenderer {
	public static void RenderObject(Data object) {
		float posx = object.getProperty("Position.x").getNumberValue();
		float posy = object.getProperty("Position.y").getNumberValue();
		float posz = object.getProperty("Position.z").getNumberValue();
		float rotx = object.getProperty("Rotation.x").getNumberValue();
		float roty = object.getProperty("Rotation.y").getNumberValue();
		float rotz = object.getProperty("Rotation.z").getNumberValue();
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

		/*glBegin(GL_QUADS);
		glColor3f(0.0f, 0.0f, 1.0f);
		glVertex3f(-1, -1, 0);
		glVertex3f(1, -1, 0);
		glVertex3f(1, 1, 0);
		glVertex3f(-1, 1, 0);*/
		
		DrawSphere(1.0, 10, 32);
		glEnd();
	}

	private static void DrawSphere(double r, int lats, int longs) {
		int i, j;

		for (i = 0; i <= lats; i++) {
			double lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
			double z0 = Math.sin(lat0);
			double zr0 = Math.cos(lat0);

			double lat1 = Math.PI * (-0.5 + (double) i / lats);
			double z1 = Math.sin(lat1);
			double zr1 = Math.cos(lat1);

			glBegin(GL_QUAD_STRIP);
			for (j = 0; j <= longs; j++) {
				double lng = 2 * Math.PI * (double) (j - 1) / longs;
				double x = Math.cos(lng);
				double y = Math.sin(lng);

				glColor3f(0.0f, 0.0f, 1.0f);
				glNormal3d(x * zr0, y * zr0, z0);
				glVertex3d(x * zr0, y * zr0, z0);
				
				glColor3f(1.0f, 0.0f, 0.0f);
				glNormal3d(x * zr1, y * zr1, z1);
				glVertex3d(x * zr1, y * zr1, z1);
			}
			glEnd();
		}
	}
}
