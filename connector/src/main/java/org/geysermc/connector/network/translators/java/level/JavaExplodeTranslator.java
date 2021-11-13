/*
 * Copyright (c) 2019-2021 GeyserMC. http://geysermc.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author GeyserMC
 * @link https://github.com/GeyserMC/Geyser
 */

package org.geysermc.connector.network.translators.java.level;

import com.github.steveice10.mc.protocol.data.game.level.block.ExplodedBlockRecord;
import com.github.steveice10.mc.protocol.packet.ingame.clientbound.level.ClientboundExplodePacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.nukkitx.protocol.bedrock.data.LevelEventType;
import com.nukkitx.protocol.bedrock.data.SoundEvent;
import com.nukkitx.protocol.bedrock.packet.LevelEventPacket;
import com.nukkitx.protocol.bedrock.packet.LevelSoundEventPacket;
import com.nukkitx.protocol.bedrock.packet.SetEntityMotionPacket;
import org.geysermc.connector.network.session.GeyserSession;
import org.geysermc.connector.network.translators.PacketTranslator;
import org.geysermc.connector.network.translators.Translator;
import org.geysermc.connector.network.translators.world.block.BlockStateValues;
import org.geysermc.connector.utils.ChunkUtils;

@Translator(packet = ClientboundExplodePacket.class)
public class JavaExplodeTranslator extends PacketTranslator<ClientboundExplodePacket> {

    @Override
    public void translate(GeyserSession session, ClientboundExplodePacket packet) {
        for (ExplodedBlockRecord record : packet.getExploded()) {
            Vector3i pos = Vector3i.from(packet.getX() + record.getX(), packet.getY() + record.getY(), packet.getZ() + record.getZ());
            ChunkUtils.updateBlock(session, BlockStateValues.JAVA_AIR_ID, pos);
        }

        Vector3f pos = Vector3f.from(packet.getX(), packet.getY(), packet.getZ());
        // Since bedrock does not play an explosion sound and particles sound, we have to manually do so
        LevelEventPacket levelEventPacket = new LevelEventPacket();
        levelEventPacket.setType(packet.getRadius() >= 2.0f ? LevelEventType.PARTICLE_HUGE_EXPLODE : LevelEventType.PARTICLE_EXPLOSION);
        levelEventPacket.setData(0);
        levelEventPacket.setPosition(pos);
        session.sendUpstreamPacket(levelEventPacket);

        LevelSoundEventPacket levelSoundEventPacket = new LevelSoundEventPacket();
        levelSoundEventPacket.setRelativeVolumeDisabled(false);
        levelSoundEventPacket.setBabySound(false);
        levelSoundEventPacket.setExtraData(-1);
        levelSoundEventPacket.setSound(SoundEvent.EXPLODE);
        levelSoundEventPacket.setIdentifier(":");
        levelSoundEventPacket.setPosition(pos);
        session.sendUpstreamPacket(levelSoundEventPacket);

        if (packet.getPushX() != 0f || packet.getPushY() != 0f || packet.getPushZ() != 0f) {
            SetEntityMotionPacket motionPacket = new SetEntityMotionPacket();
            motionPacket.setRuntimeEntityId(session.getPlayerEntity().getGeyserId());
            motionPacket.setMotion(Vector3f.from(packet.getPushX(), packet.getPushY(), packet.getPushZ()));
            session.sendUpstreamPacket(motionPacket);
        }
    }
}