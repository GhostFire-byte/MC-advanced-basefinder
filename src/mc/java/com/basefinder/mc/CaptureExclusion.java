package com.basefinder.mc;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Applies the Windows {@code WDA_EXCLUDEFROMCAPTURE} display affinity to the
 * Minecraft window, hiding the overlay from screen capture/recording. Uses the
 * Java 25 Foreign Function &amp; Memory API to call {@code user32!SetWindowDisplayAffinity}.
 * No-op and safe on non-Windows platforms.
 */
public final class CaptureExclusion {

    private static final int WDA_NONE = 0x00000000;
    private static final int WDA_EXCLUDEFROMCAPTURE = 0x00000011;

    private CaptureExclusion() {
    }

    public static boolean apply(boolean exclude) {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return false;
        }
        try {
            long glfwWindow = Minecraft.getInstance().getWindow().getWindow();
            long hwnd = GLFWNativeWin32.glfwGetWin32Window(glfwWindow);
            if (hwnd == 0L) {
                return false;
            }
            Linker linker = Linker.nativeLinker();
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32", Arena.global());
            MethodHandle setAffinity = linker.downcallHandle(
                    user32.find("SetWindowDisplayAffinity").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT));
            int result = (int) setAffinity.invoke(hwnd, exclude ? WDA_EXCLUDEFROMCAPTURE : WDA_NONE);
            return result != 0;
        } catch (Throwable t) {
            return false;
        }
    }
}
