"""Simple async event bus for cross-module state synchronization."""

import asyncio
from collections import defaultdict
from typing import Any, Callable, Coroutine


class EventBus:
    """Pub/sub event bus. Listeners are async callables."""

    def __init__(self):
        self._listeners: dict[str, list[Callable[..., Coroutine]]] = defaultdict(list)

    def on(self, event: str, callback: Callable[..., Coroutine]):
        self._listeners[event].append(callback)

    def off(self, event: str, callback: Callable[..., Coroutine]):
        if callback in self._listeners[event]:
            self._listeners[event].remove(callback)

    async def emit(self, event: str, **kwargs: Any):
        for cb in self._listeners.get(event, []):
            await cb(**kwargs)
