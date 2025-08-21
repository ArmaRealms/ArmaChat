package mineverse.Aust1n46.chat.json;

import mineverse.Aust1n46.chat.ClickAction;

import java.util.List;

public record JsonAttribute(String name, List<String> hoverText, ClickAction clickAction, String clickText) {
}
