package com.example.forge.core.uiEntities

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape

enum class ProgressShape{
    Circle,
    Square,
    Pill,
    Arch,
    Slanted,
    Pentagon,
    Gem,
    VerySunny,
    Sunny,
    Cookie4Sided,
    Cookie6Sided,
    Cookie7Sided,
    Cookie9Sided,
    Cookie12Sided,
    Clover4Leaf,
    Clover8Leaf,
    Puffy;

    companion object {
        fun getRandom(): ProgressShape {
            return entries.random()
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
val ProgressShapeEnumResolver: Map<ProgressShape, Shape>
    @Composable
    get() = mapOf(
        ProgressShape.Circle            to MaterialShapes.Circle.toShape(),
        ProgressShape.Square            to MaterialShapes.Square.toShape(),
        ProgressShape.Pill              to MaterialShapes.Pill.toShape(),
        ProgressShape.Arch              to MaterialShapes.Arch.toShape(),
        ProgressShape.Slanted           to MaterialShapes.Slanted.toShape(),
        ProgressShape.Pentagon          to MaterialShapes.Pentagon.toShape(),
        ProgressShape.Gem               to MaterialShapes.Gem.toShape(),
        ProgressShape.Sunny             to MaterialShapes.Sunny.toShape(),
        ProgressShape.VerySunny         to MaterialShapes.VerySunny.toShape(),
        ProgressShape.Cookie4Sided      to MaterialShapes.Cookie4Sided.toShape(),
        ProgressShape.Cookie7Sided      to MaterialShapes.Cookie7Sided.toShape(),
        ProgressShape.Cookie6Sided      to MaterialShapes.Cookie6Sided.toShape(),
        ProgressShape.Cookie9Sided      to MaterialShapes.Cookie9Sided.toShape(),
        ProgressShape.Cookie12Sided     to MaterialShapes.Cookie12Sided.toShape(),
        ProgressShape.Clover4Leaf       to MaterialShapes.Clover4Leaf.toShape(),
        ProgressShape.Clover8Leaf       to MaterialShapes.Clover8Leaf.toShape(),
        ProgressShape.Puffy             to MaterialShapes.Puffy.toShape(),

        )

val ProgressShapeAngleResolver: Map<ProgressShape, Float>
    @Composable
    get() = mapOf(
        ProgressShape.Circle            to 45f,
        ProgressShape.Square            to 0f,
        ProgressShape.Pill              to 85f,
        ProgressShape.Arch              to -180f,
        ProgressShape.Slanted           to 0f,
        ProgressShape.Pentagon          to 144f,
        ProgressShape.Gem               to -50f,
        ProgressShape.Sunny             to 40f,
        ProgressShape.VerySunny         to -50f,
        ProgressShape.Cookie4Sided      to 0f,
        ProgressShape.Cookie7Sided      to 130f,
        ProgressShape.Cookie6Sided      to -15f,
        ProgressShape.Cookie9Sided      to 130f,
        ProgressShape.Cookie12Sided     to 130f,
        ProgressShape.Clover4Leaf       to 130f,
        ProgressShape.Clover8Leaf       to 130f,
        ProgressShape.Puffy             to 130f,
        )