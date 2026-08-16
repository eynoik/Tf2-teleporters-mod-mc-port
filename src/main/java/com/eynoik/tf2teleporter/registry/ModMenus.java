package com.eynoik.tf2teleporter.registry;

import com.eynoik.tf2teleporter.TF2TeleporterMod;
import com.eynoik.tf2teleporter.menu.TeleporterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TF2TeleporterMod.MOD_ID);

    public static final Supplier<MenuType<TeleporterMenu>> TELEPORTER = MENUS.register(
            "teleporter",
            () -> IMenuTypeExtension.create(TeleporterMenu::new)
    );

    private ModMenus() {
    }
}
