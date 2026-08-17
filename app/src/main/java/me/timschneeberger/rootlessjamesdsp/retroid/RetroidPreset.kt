package me.timschneeberger.rootlessjamesdsp.retroid

data class RetroidPreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val graphicEq: String
)

object RetroidPresets {
    val pocket5 = RetroidPreset(
        id = "pocket5",
        title = "Retroid Pocket 5",
        subtitle = "Speaker profile",
        graphicEq = "GraphicEQ: 480 0; 600 -5; 700 -16; 1000 -18; 1200 -10; 1670 -10; 2160 -18; 2800 -18; 3800 -28; 5000 -7; 7000 0;"
    )

    val pocket6 = RetroidPreset(
        id = "pocket6",
        title = "Retroid Pocket 6",
        subtitle = "Speaker profile",
        graphicEq = "GraphicEQ: 62 -11; 125 -8.8; 250 -0.6; 350 0.4; 1000 -15.6; 1500 -12.6; 2000 -14.9; 4000 -16.3; 8000 -2.5; 10500 -2.5; 16000 -1;"
    )

    val flip2Normal = RetroidPreset(
        id = "flip2_normal",
        title = "Flip 2 Normal",
        subtitle = "Balanced profile",
        graphicEq = "GraphicEQ: 347 0; 450 -4.5; 500 -8 ; 600 -4.5; 1015 -17.5; 1500 -13; 2300 -8; 4000 -8.5; 6000 -13.5; 7000 -16.5; 8500 -22.5; 11000 -12; 12000 -4; 16000 0;"
    )

    val flip2Quiet = RetroidPreset(
        id = "flip2_quiet",
        title = "Flip 2 Quiet",
        subtitle = "Slightly lower volume",
        graphicEq = "GraphicEQ: 347 -2; 450 -6.5; 500 -10; 600 -6.5; 1015 -19.5; 1500 -15; 2300 -10; 4000 -10.5; 6000 -15.5; 7000 -18.5; 8500 -24.5; 11000 -14; 12000 -6; 16000 -2;"
    )

    val flip2Sparkle2 = RetroidPreset(
        id = "flip2_sparkle2",
        title = "Flip 2 Sparkle",
        subtitle = "Stronger correction",
        graphicEq = "GraphicEQ: 25 -3.00; 63 -1.50; 100 0.00; 347 -1.48; 450 -7.385; 500 -11.285; 600 -8.00; 1015 -21.60; 1500 -16.18; 2300 -9.95; 4000 -8.75; 6000 -13.25; 7000 -16.13; 8500 -21.94; 11000 -11.42; 12000 -3.59; 16000 -0.25;"
    )

    val all = listOf(pocket5, pocket6, flip2Normal, flip2Quiet, flip2Sparkle2)
}
