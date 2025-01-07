from graia.ariadne.app import Ariadne
from graia.ariadne.event.message import FriendMessage
from graia.ariadne.message.parser.twilight import Twilight, FullMatch, UnionMatch
from graia.ariadne.model import Friend

from graia.saya import Channel, Saya
from graia.saya.builtins.broadcast.schema import ListenerSchema

from ...utils import config
from loguru import logger

prefix = config.get("COMMAND_PREFIX")
master_qq = config.get("MASTER_QQ")
custom_commands = config.get("CUSTOM_COMMANDS_PACKAGE")

channel = Channel.current()
saya = Saya.current()


@channel.use(
    ListenerSchema(
        listening_events=[FriendMessage],
        inline_dispatchers=[Twilight(
            FullMatch(prefix),
            UnionMatch("重载插件", "reload_plugins")
        )]
    )
)
async def _ReloadPlugins(app: Ariadne, friend: Friend):
    logger.info(f"触发命令：重载插件 qq[{friend.id}]({friend.nickname})")
    if master_qq != "" and friend.id != master_qq:
        logger.warning(f"重载插件命令主人专用，其他人不可触发")
        return
    if custom_commands is None or custom_commands == "":
        logger.info(f"自定义命令为设置，无需重载")
        return
    _channel = saya.channels.get(custom_commands)
    saya.reload_channel(_channel)
    logger.success(f"自定义命令({custom_commands})重载成功")
    return await app.send_message(friend, f"自定义命令({custom_commands})重载成功")
