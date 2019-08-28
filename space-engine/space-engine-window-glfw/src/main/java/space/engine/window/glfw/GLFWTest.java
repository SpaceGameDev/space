package space.engine.window.glfw;

import org.jetbrains.annotations.NotNull;
import space.engine.Side;
import space.engine.barrier.Barrier;
import space.engine.barrier.future.Future;
import space.engine.event.EventEntry;
import space.engine.freeableStorage.FreeableStorageCleaner;
import space.engine.key.attribute.AttributeList;
import space.engine.key.attribute.AttributeListModify;
import space.engine.logger.BaseLogger;
import space.engine.logger.LogLevel;
import space.engine.window.InputDevice.Keyboard;
import space.engine.window.Keycode;
import space.engine.window.Window;
import space.engine.window.WindowContext;
import space.engine.window.WindowContext.*;
import space.engine.window.WindowFramework;
import space.engine.window.extensions.VideoModeDesktopExtension;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.*;
import static space.engine.Empties.EMPTY_OBJECT_ARRAY;
import static space.engine.barrier.Barrier.*;
import static space.engine.window.Window.*;
import static space.engine.window.WindowContext.*;
import static space.engine.window.extensions.BorderlessExtension.BORDERLESS;
import static space.engine.window.extensions.VideoModeDesktopExtension.*;
import static space.engine.window.extensions.VideoModeExtension.HEIGHT;

public class GLFWTest {
	
	public static boolean CRASH = false;
	public static int SECONDS = 300000000;
	public static final int WINDOW_CNT = 5;
	public static boolean FREE_WINDOW = true;
	public static ExampleDraw exampleDraw = ExampleDraw.ROTATING_CUBE;
	
	public static final double MULTIPLIER = (2 * PI) / 3;
	public static final double OFFSET0 = 0;
	public static final double OFFSET1 = (2 * PI) / 3;
	public static final double OFFSET2 = OFFSET1 * 2;
	
	public static void main(String[] args) throws Exception {
		System.setProperty("org.lwjgl.util.NoChecks", "true");
		
		//logger
		BaseLogger logger = new BaseLogger();
		BaseLogger.defaultHandler(logger);
		BaseLogger.defaultPrinter(logger);
		
		//cleaner
		FreeableStorageCleaner.setCleanupLogger(logger);
		FreeableStorageCleaner.startCleanupThread();
		
		//framework
		WindowFramework windowfw = new GLFWWindowFramework();
		
		//context
		AttributeListModify<WindowContext> windowContextAttInitial = WindowContext.CREATOR.createModify();
		windowContextAttInitial.put(API_TYPE, OpenGLApiType.GL);
		windowContextAttInitial.put(GL_VERSION_MAJOR, 3);
		windowContextAttInitial.put(GL_VERSION_MINOR, 0);
		windowContextAttInitial.put(GL_FORWARD_COMPATIBLE, false);
		WindowContext context = windowfw.createContext(windowContextAttInitial.createNewAttributeList(), EMPTY_OBJECT_ARRAY).awaitGet();
		
		context.getInputDevices().addHookAsStartedEmpty(change -> change.added().stream()
																		.filter(Keyboard.class::isInstance).map(Keyboard.class::cast)
																		.forEach(o -> {
																			o.getCharacterInputEvent().addHook(System.out::print);
																			o.getKeyInputEvent().addHook((key, wasPressed) -> {
																				if (wasPressed && key == Keycode.KEY_ENTER)
																					System.out.println();
																			});
																		}));
		
		//window
		AttributeListModify<Window> windowAttInitial = Window.CREATOR.createModify();
		windowAttInitial.put(VIDEO_MODE, VideoModeDesktopExtension.class);
		windowAttInitial.put(WIDTH, 1080);
		windowAttInitial.put(HEIGHT, 1080);
//		windowAttInitial.put(POS_X, 0);
//		windowAttInitial.put(POS_Y, 0);
		windowAttInitial.put(HAS_TRANSPARENCY, true);
		windowAttInitial.put(BORDERLESS, true);
		windowAttInitial.put(TITLE, "GLFWTest Window");
		AttributeList<Window> windowAtt = windowAttInitial.createNewAttributeList();
		Set<? extends Window> windows = IntStream.range(0, WINDOW_CNT).mapToObj(i -> context.createWindow(windowAtt, EMPTY_OBJECT_ARRAY)).map(window -> {
			try {
				return window.awaitGet();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toMap(Function.identity(), o -> true, (o, o2) -> o, ConcurrentHashMap::new)).keySet(true);
		windows.forEach(window -> window.getWindowCloseEvent().addHook(new EventEntry<>(window1 -> {
			//FIXME this is a bit scary, though shouldn't break as long as we don't do continuous event polling
			windows.remove(window1);
			window1.free();
		})));
		
		if (CRASH)
			throw new RuntimeException("Test Crash!");
		
		FboInfo fboInfo = createFbo(context, 1080, 1080).awaitGet();
		
		for (int i = 0; i < SECONDS * 60 && windows.size() != 0; i++) {
			
			int i2 = i;
			Barrier draw = nowRun(context, () -> {
				glBindFramebuffer(GL_FRAMEBUFFER, fboInfo.fboId);
				glViewport(0, 0, 1080, 1080);
				glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
				exampleDraw.run.accept(i2 / 60f);
				glBindFramebuffer(GL_FRAMEBUFFER, 0);
				glFinish();
			});
			draw.thenStart(() -> when(windows.stream().map(w -> w.openGL_SwapBuffer(fboInfo.texId)))).await();
			
			Thread.sleep(1000 / 60);
			
			//changing out window
//			if (i % 600 == 299) {
//				System.out.println("windowed");
//				AttributeListModify<Window> modify = windowAtt.createModify();
//				modify.put(HAS_TRANSPARENCY, false);
//				modify.put(BORDERLESS, false);
//				modify.apply().await();
//			} else if (i % 600 == 599) {
//				System.out.println("transparent");
//				AttributeListModify<Window> modify = windowAtt.createModify();
//				modify.put(HAS_TRANSPARENCY, true);
//				modify.put(BORDERLESS, true);
//				modify.apply().await();
//			}

//			AttributeListModify<Window> modify = windowAtt.createModify();
//			modify.put(POS_X, modify.get(POS_X) + 1);
//			modify.put(POS_Y, modify.get(POS_Y) + 1);
//			modify.apply().await();
		}
		
		deleteFbo(context, fboInfo).await();
		
		if (FREE_WINDOW) {
			windows.forEach(Window::free);
			windowfw.free();
		}
		logger.log(LogLevel.INFO, "Exit!");
		Side.exit().awaitUninterrupted();
	}
	
	public static class FboInfo {
		
		public final int texId;
		public final int fboId;
		
		public FboInfo(int texId, int fboId) {
			this.texId = texId;
			this.fboId = fboId;
		}
	}
	
	private static @NotNull Future<FboInfo> createFbo(WindowContext context, int width, int height) {
		return nowFuture(context, () -> {
			int tex = glGenTextures();
			glBindTexture(GL_TEXTURE_2D, tex);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
			glBindTexture(GL_TEXTURE_2D, 0);
			
			int fbo = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, fbo);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex, 0);
			glDrawBuffer(GL_COLOR_ATTACHMENT0);
			
			int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
				glDeleteFramebuffers(fbo);
				glDeleteTextures(tex);
				throw new RuntimeException("FBO status: " + Integer.toHexString(fboStatus));
			}
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			
			return new FboInfo(tex, fbo);
		});
	}
	
	private static @NotNull Barrier deleteFbo(WindowContext context, FboInfo info) {
		return nowRun(context, () -> {
			glDeleteFramebuffers(info.fboId);
			glDeleteTextures(info.texId);
		});
	}
	
	public enum ExampleDraw {
		
		COLORED_TRIANGLE(f -> {
			glColor3f((float) sin(f * MULTIPLIER + OFFSET0), (float) sin(f * MULTIPLIER + OFFSET1), (float) sin(f * MULTIPLIER + OFFSET2));
			glBegin(GL_TRIANGLES);
			glVertex2f(0.0f, 0.5f);
			glVertex2f(0.5f, -0.5f);
			glVertex2f(-0.5f, -0.5f);
			glEnd();
		}),
		
		ROTATING_CUBE(f -> {
			glPushMatrix();
			glScalef(0.5f, 0.5f, 0.5f);
			glRotatef(f * 90f, 0, 1, 0);
			glRotatef(30, 1, 0, 0);
			
			float[] floats = {
					//@formatter:off
					//1
					-1, -1, 1,
					-1, 1, 1,
					-1, 1, -1,
					-1, -1, -1,
					
					//2
					1, -1, 1,
					1, 1, 1,
					1, 1, -1,
					1, -1, -1,
					
					//3
					1, -1, -1,
					1, -1, 1,
					-1, -1, 1,
					-1, -1, -1,
					
					//4
					1, 1, -1,
					1, 1, 1,
					-1, 1, 1,
					-1, 1, -1,
					
					//5
					-1, -1, -1,
					-1, 1, -1,
					1, 1, -1,
					1, -1, -1,
					
					//6
					-1, -1, 1,
					-1, 1, 1,
					1, 1, 1,
					1, -1, 1,
					//@formatter:on
			};
			
			glColor3f((float) sin(f * MULTIPLIER + OFFSET0), (float) sin(f * MULTIPLIER + OFFSET1), (float) sin(f * MULTIPLIER + OFFSET2));
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glBegin(GL_QUADS);
			for (int i = 0; i < floats.length; i += 3)
				glVertex3f(floats[i], floats[i + 1], floats[i + 2]);
			glEnd();
			
			glColor3f(1, 1, 1);
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			glBegin(GL_QUADS);
			for (int i = 0; i < floats.length; i += 3)
				glVertex3f(floats[i], floats[i + 1], floats[i + 2]);
			glEnd();
			
			glPopMatrix();
		});
		
		public final Consumer<Float> run;
		
		ExampleDraw(Consumer<Float> run) {
			this.run = run;
		}
	}
}
