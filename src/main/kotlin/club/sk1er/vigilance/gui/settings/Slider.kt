package club.sk1er.vigilance.gui.settings

import club.sk1er.elementa.components.UIBlock
import club.sk1er.elementa.components.UIContainer
import club.sk1er.elementa.constraints.*
import club.sk1er.elementa.dsl.*
import club.sk1er.elementa.effects.OutlineEffect
import club.sk1er.elementa.state.toConstraint
import club.sk1er.vigilance.gui.VigilancePalette
import java.awt.Color

class Slider(initialValue: Float) : UIContainer() {
    private var percentage = initialValue
    private var onValueChange: (Float) -> Unit = {}
    private var dragging = false
    private var grabOffset = 0f

    private val outerBox = UIContainer().constrain {
        x = basicXConstraint {
            this@Slider.getLeft() + 1f + this@Slider.getHeight() * 0.75f
        }
        y = 1.pixels()
        width = basicWidthConstraint {
            this@Slider.getWidth() - 2f - this@Slider.getHeight() * 1.5f
        }
        height = RelativeConstraint(0.5f)
    } childOf this effect OutlineEffect(VigilancePalette.BRIGHT_DIVIDER, 0.5f).bindColor(VigilancePalette.brightDividerState)

    private val completionBox = UIBlock().constrain {
        x = (-0.5f).pixels()
        y = (-0.5f).pixels()
        width = RelativeConstraint(percentage)
        height = RelativeConstraint(1f) + 1.pixels()
        color = VigilancePalette.accentState.toConstraint()
    } childOf outerBox

    val grabBox = UIBlock().constrain {
        x = basicXConstraint { completionBox.getRight() - it.getWidth() / 2f }
        y = CenterConstraint() boundTo outerBox
        width = AspectConstraint(1f)
        height = 100.percent()
        color = VigilancePalette.accentState.toConstraint()
    } childOf this effect OutlineEffect(Color.BLACK, 0.5f)

    init {
        grabBox.onMouseClick { event ->
            if (event.mouseButton == 0) {
                dragging = true
                grabOffset = event.relativeX - (grabBox.getWidth() / 2)
                event.stopPropagation()
            }
        }.onMouseRelease {
            dragging = false
            grabOffset = 0f
        }.onMouseDrag { mouseX, _, _ ->
            if (!dragging) return@onMouseDrag

            val clamped = (mouseX + grabBox.getLeft() - grabOffset).coerceIn(outerBox.getLeft()..outerBox.getRight())
            val percentage = (clamped - outerBox.getLeft()) / outerBox.getWidth()
            setCurrentPercentage(percentage)
        }

        outerBox.onMouseClick { event ->
            if (event.mouseButton == 0) {
                val percentage = event.relativeX / outerBox.getWidth()
                setCurrentPercentage(percentage)
                dragging = true
            }
        }
    }

    fun getCurrentPercentage() = percentage

    fun setCurrentPercentage(newPercentage: Float, callListener: Boolean = true) {
        percentage = newPercentage.coerceIn(0f..1f)

        completionBox.setWidth(RelativeConstraint(percentage))

        if (callListener) {
            onValueChange(percentage)
        }
    }

    fun onValueChange(listener: (Float) -> Unit) {
        onValueChange = listener
    }
}
