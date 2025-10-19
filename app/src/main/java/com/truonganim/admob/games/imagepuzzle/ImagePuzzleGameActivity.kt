package com.truonganim.admob.games.imagepuzzle

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameResult
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImagePuzzleGameActivity : ComponentActivity() {

    private var gameId: String = ""
    private var inputImages: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get game data from intent
        gameId = intent.getStringExtra(Game.EXTRA_GAME_ID) ?: ""
        inputImages = intent.getStringArrayListExtra(Game.EXTRA_INPUT_IMAGES) ?: emptyList()

        setContent {
            AdMobBaseTheme {
                ImagePuzzleGameScreen(
                    imageUrl = inputImages.firstOrNull() ?: "",
                    onWin = { finishWithResult(GameResult.WIN) },
                    onClose = { finishWithResult(GameResult.CANCELLED) }
                )
            }
        }
    }

    private fun finishWithResult(result: GameResult) {
        val resultIntent = Intent().apply {
            putExtra(Game.EXTRA_GAME_RESULT, result)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePuzzleGameScreen(
    imageUrl: String,
    onWin: () -> Unit,
    onClose: () -> Unit
) {
    var showWinDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val grid = 2
    val tileSize = DpSize(200.dp, 200.dp)

    var img by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Tải ảnh từ URL -> ImageBitmap
    LaunchedEffect(imageUrl) {
        isLoading = true
        error = null
        img = try {
            if (imageUrl.isBlank()) {
                error = "Image URL is empty"
                null
            } else {
                loadImageBitmap(context, imageUrl)
            }
        } catch (e: Exception) {
            error = e.message ?: "Load image error"
            null
        }
        isLoading = false
    }

    // tiles[pos] = tileIndex (mảnh đúng ở vị trí pos)
    var tiles by remember { mutableStateOf((0 until grid * grid).toList().shuffled()) }
    var firstPick by remember { mutableStateOf<Int?>(null) }
    val solved = tiles.withIndex().all { (pos, idx) -> pos == idx }

    // Gọi onWin khi hoàn thành lần đầu
    var hasReportedWin by remember { mutableStateOf(false) }
    LaunchedEffect(solved) {
        if (solved && !hasReportedWin) {
            hasReportedWin = true
            showWinDialog = true
        }
    }

    fun shuffle() {
        tiles = (0 until grid * grid).shuffled()
        firstPick = null
        hasReportedWin = false
    }

    // Win Dialog
    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "🎉 You Win!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Congratulations! You successfully complete the image!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = onWin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Image Puzzle",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text(error ?: "Error", color = MaterialTheme.colorScheme.error)
                img == null -> Text("No image")
                else -> {
                    val image = img!!
                    val tileW = image.width / grid
                    val tileH = image.height / grid

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Lưới 3x3
                        for (row in 0 until grid) {
                            Row {
                                for (col in 0 until grid) {
                                    val pos = row * grid + col
                                    val tileIndex = tiles[pos]
                                    val srcRow = tileIndex / grid
                                    val srcCol = tileIndex % grid

                                    // 🔹 Highlight logic
                                    val isSelected = pos == firstPick


                                    val borderColor by animateColorAsState(
                                        targetValue = when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.White.copy(alpha = 0.6f)
                                        },
                                        animationSpec = tween(200)
                                    )

                                    val overlayColor by animateColorAsState(
                                        targetValue = when {
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.18f
                                            )

                                            else -> Color.Transparent
                                        },
                                        animationSpec = tween(200)
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .size(tileSize.width, tileSize.height)
                                            .padding(2.dp)
                                            .clickable {
                                                if (firstPick == null) {
                                                    firstPick = pos
                                                } else {
                                                    val second = pos
                                                    if (second != firstPick) {
                                                        val a = firstPick!!
                                                        if (isAdjacent(a, second, grid)) {
                                                            val list = tiles.toMutableList()
                                                            list[a] = tiles[second]
                                                            list[second] = tiles[a]
                                                            tiles = list
                                                        }
                                                    }
                                                    firstPick = null
                                                }
                                            },
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(2.dp, borderColor),
                                        color = Color.Transparent,
                                        shadowElevation = 2.dp
                                    ) {
                                        Box {
                                            Canvas(Modifier.fillMaxSize()) {
                                                val src = Rect(
                                                    (srcCol * tileW).toFloat(),
                                                    (srcRow * tileH).toFloat(),
                                                    ((srcCol + 1) * tileW).toFloat(),
                                                    ((srcRow + 1) * tileH).toFloat()
                                                )
                                                drawImage(
                                                    image = image,
                                                    srcOffset = androidx.compose.ui.unit.IntOffset(
                                                        src.left.toInt(),
                                                        src.top.toInt()
                                                    ),
                                                    srcSize = androidx.compose.ui.unit.IntSize(
                                                        (src.right - src.left).toInt(),
                                                        (src.bottom - src.top).toInt()
                                                    ),
                                                    dstSize = androidx.compose.ui.unit.IntSize(
                                                        size.width.toInt(), size.height.toInt()
                                                    )
                                                )
                                            }

                                            // 🔹 Overlay mờ highlight
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(overlayColor)
                                            )
                                        }
                                    }
                                }

                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(onClick = ::shuffle) { Text("Shuffle") }

                        AnimatedVisibility(visible = solved) {
                            Text(
                                text = "🎉 Completed!",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------- Helpers ------------------- */

private suspend fun loadImageBitmap(
    context: android.content.Context,
    url: String
): ImageBitmap? = withContext(Dispatchers.IO) {
    val loader = ImageLoader(context)
    val req = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false) // cần Bitmap thường để cắt
        .build()
    val result = loader.execute(req)
    if (result is SuccessResult) {
        val dr = result.drawable
        val bmp = when (dr) {
            is android.graphics.drawable.BitmapDrawable -> dr.bitmap
            is android.graphics.drawable.VectorDrawable -> dr.toBitmapCompat()
            else -> dr.toBitmapCompat()
        }
        bmp.asImageBitmap()
    } else null
}

private fun android.graphics.drawable.Drawable.toBitmapCompat(): Bitmap {
    val w = intrinsicWidth.coerceAtLeast(1)
    val h = intrinsicHeight.coerceAtLeast(1)
    val bmp = createBitmap(w, h)
    val canvas = android.graphics.Canvas(bmp)
    setBounds(0, 0, w, h)
    draw(canvas)
    return bmp
}

private fun isAdjacent(a: Int, b: Int, grid: Int): Boolean {
    val r1 = a / grid;
    val c1 = a % grid
    val r2 = b / grid;
    val c2 = b % grid
    // Kề nhau nếu chênh 1 ô theo hàng hoặc cột (không chéo)
    return (kotlin.math.abs(r1 - r2) + kotlin.math.abs(c1 - c2)) == 1
}
