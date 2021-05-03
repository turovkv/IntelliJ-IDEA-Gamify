package com.intellij.plugin.gamification.listeners;

import com.intellij.util.messages.Topic;

public interface GameEventListener {
    Topic<GameEventListener> TOPIC = new Topic<>(GameEventListener.class);

    /**
     * Called when a level has changed.
     */
    default void levelChanged() { }

    /**
     * Called when a progress has changed.
     */
    default void progressChanged() { }
}
