from graia.ariadne.message.chain import MessageChain
from graia.ariadne.message.element import At, AtAll, Quote


def check_at_object(account: int, message: MessageChain):
    for element in message.content:
        if isinstance(element, Quote) or isinstance(element, AtAll):
            # 忽略atall和引用回复消息
            return False
        if isinstance(element, At):
            if element.target != account:
                # 忽略at其他人消息
                return False
    return True
