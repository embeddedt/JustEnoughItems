package mezz.jei.network.packets;

import mezz.jei.common.network.IPacketId;
import mezz.jei.common.network.PacketIdServer;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.common.network.packets.PacketJei;
import mezz.jei.common.network.ServerPacketData;
import mezz.jei.core.config.IServerConfig;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.util.ServerCommandUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketRequestCheatPermission extends PacketJei {
	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.CHEAT_PERMISSION_REQUEST;
	}

	@Override
	public void writePacketData(FriendlyByteBuf buf) {
		// the packet itself is the only data needed
	}

	public static void readPacketData(ServerPacketData data) {
		ServerPlayer player = data.player();
		IServerConfig serverConfig = data.serverConfig();
		boolean hasPermission = ServerCommandUtil.hasPermissionForCheatMode(player, serverConfig);
		PacketCheatPermission packetCheatPermission = new PacketCheatPermission(hasPermission);

		IConnectionToClient connection = data.connection();
		connection.sendPacketToClient(packetCheatPermission, player);
	}
}
