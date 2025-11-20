package relake.common.util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SoundUtil {
    private static AudioInputStream lastCreatedStream;
    private static final CopyOnWriteArrayList<Clip> CLIPS_LIST = new CopyOnWriteArrayList<>();
    private static final String packagePath = "/assets/minecraft/relake/sounds/";

    public static void playSound(final String location, float volume) {
        if (!System.getProperty("os.name").startsWith("Windows")) return;
        CompletableFuture.runAsync(() -> {
            if ((lastCreatedStream = getAudioInputStreamAsResLoc(packagePath + location)) == null) return;
            final Clip createdClip;
            CLIPS_LIST.stream().filter(Line::isOpen).filter(clip -> !clip.isRunning()).forEach(Line::close);
            CLIPS_LIST.removeIf(clip -> !clip.isRunning());
            if ((createdClip = createClip(lastCreatedStream)) != null) CLIPS_LIST.add(createdClip);
            CLIPS_LIST.stream().filter(Objects::nonNull).filter(clip -> !clip.isOpen()).forEach(clip -> {
                try {
                    clip.open(lastCreatedStream);
                    setClipVolume(clip, volume);
                    clip.start();
                } catch (LineUnavailableException | IOException LUE) {
                    LUE.fillInStackTrace();
                }
            });
        });
    }

    public static void playSoundInstant(final String location, float volume) {
        if (!System.getProperty("os.name").startsWith("Windows")) return;
        if ((lastCreatedStream = getAudioInputStreamAsResLoc(packagePath + location)) == null) return;
        final Clip createdClip;
        if ((createdClip = createClip(lastCreatedStream)) != null) CLIPS_LIST.add(createdClip);
        try {
            createdClip.open(lastCreatedStream);
            setClipVolume(createdClip, volume);
            createdClip.start();
        } catch (LineUnavailableException | IOException LUE) {
            LUE.fillInStackTrace();
        }
    }

    public static void playSound(final String location) {
        playSound(location, .45F);
    }

    public static void playSoundInstant(final String location) {
        playSoundInstant(location, .45F);
    }

    private static AudioFormat prevFormat;
    private static DataLine.Info lastData;

    private static Clip createClip(AudioInputStream stream) {
        final AudioFormat format = stream.getFormat();
        if (prevFormat != format) {
            lastData = new DataLine.Info(Clip.class, stream.getFormat());
            prevFormat = format;
        }
        try {
            return (Clip) AudioSystem.getLine(lastData);
        } catch (LineUnavailableException LUE) {
            LUE.fillInStackTrace();
            return null;
        }
    }

    private static void setClipVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        final FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        volumeControl.setValue((float) (Math.log(Math.max(Math.min(volume, 1.D), 0.D)) / Math.log(10.D) * 20.D));
    }

    private static AudioInputStream getAudioInputStreamAsResLoc(String resLoc) {
        try {
            return AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(SoundUtil.class.getResourceAsStream(resLoc))));
        } catch (UnsupportedAudioFileException | IOException ULT) {
            ULT.fillInStackTrace();
            return null;
        }
    }
}
