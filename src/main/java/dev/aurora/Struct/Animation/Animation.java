package dev.aurora.Struct.Animation;

import dev.aurora.Struct.Animation.API.IAnimation;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Animation implements IAnimation {
    private final List<Frame> frames;
    private final boolean loop;
    private int currentFrame;

    public Animation(boolean loop) {
        this.frames = new ArrayList<>();
        this.loop = loop;
        this.currentFrame = 0;
    }

    public Animation addFrame(Frame frame) {
        frames.add(frame);
        return this;
    }

    @Override
    public ItemStack getNextItem() {
        if (frames.isEmpty()) return null;
        Frame frame = frames.get(currentFrame);
        return frame.getItem();
    }

    @Override
    public Consumer<InventoryClickEvent> getClickAction() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrame).getClickAction();
    }

    @Override
    public int getDuration() {
        if (frames.isEmpty()) return 20; // Default duration
        return frames.get(currentFrame).getDuration();
    }

    @Override
    public boolean shouldContinue() {
        if (frames.isEmpty()) return false;
        currentFrame++;
        if (currentFrame >= frames.size()) {
            if (loop) {
                currentFrame = 0;
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        currentFrame = 0;
    }
}