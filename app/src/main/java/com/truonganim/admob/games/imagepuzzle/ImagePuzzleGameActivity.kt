package com.truonganim.admob.games.imagepuzzle

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.truonganim.admob.data.Game
import com.truonganim.admob.data.GameResult
import com.truonganim.admob.ui.theme.AdMobBaseTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// ==============================
// Single-file Sliding Puzzle Game
// ==============================

private const val BLANK = -1

private data class PuzzleState(
    val grid: Int,
    val tiles: List<Int>, // 0..(n-2) + BLANK
    val moves: Int = 0,
    val seconds: Int = 0,
    val running: Boolean = true
) {
    val isSolved: Boolean
        get() = tiles.withIndex().all { (i, v) ->
            (i < tiles.lastIndex && v == i) || (i == tiles.lastIndex && v == BLANK)
        }
}

class ImagePuzzleGameActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val inputImages = intent.getStringArrayListExtra(Game.EXTRA_INPUT_IMAGES) ?: emptyList()

        setContent {
            AdMobBaseTheme {
                ImagePuzzleScreen(
                    imageUrl = inputImages.firstOrNull() ?: "",
                    onWin = { finishWithResult(GameResult.WIN) },
                    onClose = { finishWithResult(GameResult.CANCELLED) }
                )
            }
        }
    }

    private fun finishWithResult(result: GameResult) {
        val resultIntent = Intent().apply { putExtra(Game.EXTRA_GAME_RESULT, result) }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePuzzleScreen(
    imageUrl: String,
    onWin: () -> Unit,
    onClose: () -> Unit,
    imageOverride: ImageBitmap? = null
) {
//    var grid by remember { mutableIntStateOf(3) }                  // 2..5
    val grid = 5
    var img by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(true) }
    var showTutorial by remember { mutableStateOf(true) }
    var isStarted by remember { mutableStateOf(false) }
    var showWinDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(imageUrl, imageOverride) {
        isLoading = true; error = null
        img = try {
            when {
                imageOverride != null -> imageOverride
                imageUrl.isBlank() -> null
                else -> loadSquareImageBitmap(context, imageUrl)
            }
        } catch (e: Exception) {
            error = e.message; null
        }
        isLoading = false
    }


    // Puzzle state
    var state by remember(grid) {
        mutableStateOf(PuzzleState(grid = grid, tiles = shuffleSolvable(grid)))
    }

    // Timer
    LaunchedEffect(isStarted, state.running, state.isSolved) {
        if (!isStarted || !state.running || state.isSolved) return@LaunchedEffect
        while (true) {
            delay(1000)
            if (!isStarted || !state.running || state.isSolved) break
            state = state.copy(seconds = state.seconds + 1)
        }
    }

    // Win dialog trigger
    LaunchedEffect(state.isSolved) {
        if (state.isSolved) showWinDialog = true
    }

    fun newGame() {
        state = PuzzleState(grid = grid, tiles = shuffleSolvable(grid))
        showWinDialog = false
    }

    if (showTutorial) {
        AlertDialog(
            onDismissRequest = { showTutorial = false },
            title = {
                Text(
                    text = "🎮 Mini Game Challenge",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column {
                    Text("A secret album is sealed away behind this puzzle.")
                    Spacer(Modifier.height(6.dp))
                    Text("🎯 Goal: Slide the tiles to rebuild the picture and unlock it!")
                    Spacer(Modifier.height(6.dp))
                    Text("🕹 Tap tiles beside the empty space to move them.")
                    Spacer(Modifier.height(6.dp))
                    Text("💡 Finish fast to prove your puzzle skills!")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTutorial = false
                        isStarted = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Start Now!") }
            }
        )
    }

    if (showWinDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    text = "🎉 Album Unlocked!",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Congratulations! You’ve solved the puzzle and unlocked the hidden photo album!",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "✨ New memories have been added to your collection!",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Moves: ${state.moves} • Time: ${state.seconds}s",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onWin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View My Album")
                }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Image Puzzle",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {

                        // Preview ảnh to (bên dưới TopBar)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 1.dp,
                            border = BorderStroke(1.dp, Color.White.copy(0.4f)),
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                        ) {
                            Image(
                                bitmap = img!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))


                        Spacer(Modifier.height(8.dp))

                        // Status
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Moves: ${state.moves}",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Time: ${state.seconds}s",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Board (square, responsive)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val boardSize = maxWidth
                                    val tileSize = boardSize / grid
                                    val image = img!!
                                    val tileW = image.width / grid
                                    val tileH = image.height / grid

                                    Column(modifier = Modifier.size(boardSize)) {
                                        for (r in 0 until grid) {
                                            Row {
                                                for (c in 0 until grid) {
                                                    val pos = r * grid + c
                                                    val v = state.tiles[pos]

                                                    Surface(
                                                        modifier = Modifier
                                                            .size(tileSize)
                                                            .padding(1.dp)
                                                            .clickable(enabled = !state.isSolved && v != BLANK) {
                                                                val blank =
                                                                    state.tiles.indexOf(BLANK)
                                                                if (neighbors(pos, grid).contains(
                                                                        blank
                                                                    )
                                                                ) {
                                                                    val list =
                                                                        state.tiles.toMutableList()
                                                                    list[blank] = v
                                                                    list[pos] = BLANK
                                                                    state = state.copy(
                                                                        tiles = list,
                                                                        moves = state.moves + 1
                                                                    )
                                                                }
                                                            },
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            Color.White.copy(0.5f)
                                                        ),
                                                        color = if (v == BLANK) Color.Transparent else Color.Black
                                                    ) {
                                                        if (v != BLANK) {
                                                            val srcRow = v / grid
                                                            val srcCol = v % grid
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
                                                                        (src.width).toInt(),
                                                                        (src.height).toInt()
                                                                    ),
                                                                    dstSize = androidx.compose.ui.unit.IntSize(
                                                                        size.width.toInt(),
                                                                        size.height.toInt()
                                                                    )
                                                                )
                                                            }
                                                        } else {
                                                            Box(
                                                                Modifier
                                                                    .fillMaxSize()
                                                                    .background(Color.Transparent)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))


                        // Controls: chỉ còn nút Shuffle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(onClick = { newGame() }) { Text("Shuffle") }
                        }

                        AnimatedVisibility(visible = state.isSolved) {
                            Text(
                                text = "🎉 Completed!",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 10.dp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImagePuzzlePreview() {
    AdMobBaseTheme {
        ImagePuzzleScreen(
            imageUrl = "",
            onWin = {},
            onClose = {},
            imageOverride = makePreviewBitmap().asImageBitmap()
        )
    }
}

/* ================= Helpers (logic + image) ================= */

private fun solvedTiles(grid: Int): List<Int> {
    val total = grid * grid
    return (0 until total - 1).toList() + BLANK
}

private fun neighbors(index: Int, grid: Int): List<Int> {
    val r = index / grid
    val c = index % grid
    val res = mutableListOf<Int>()
    if (r > 0) res += index - grid
    if (r < grid - 1) res += index + grid
    if (c > 0) res += index - 1
    if (c < grid - 1) res += index + 1
    return res
}

private fun isSolvable(tiles: List<Int>, grid: Int): Boolean {
    val arr = tiles.filter { it != BLANK }
    var inv = 0
    for (i in arr.indices) for (j in i + 1 until arr.size) if (arr[i] > arr[j]) inv++
    return if (grid % 2 == 1) {
        inv % 2 == 0
    } else {
        val blankRowFromBottom = grid - (tiles.indexOf(BLANK) / grid)
        (blankRowFromBottom % 2 == 0) xor (inv % 2 == 0)
    }
}

private fun shuffleSolvable(grid: Int): List<Int> {
    val base = solvedTiles(grid)
    var s: List<Int>
    do {
        s = base.shuffled()
    } while (!isSolvable(s, grid) || s == base)
    return s
}

private suspend fun loadSquareImageBitmap(
    context: android.content.Context,
    url: String,
    maxSize: Int = 2048
): ImageBitmap? = withContext(Dispatchers.IO) {
    val loader = ImageLoader(context)
    val req = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .size(maxSize) // downscale trước
        .build()
    val result = loader.execute(req)
    if (result is SuccessResult) {
        val dr = result.drawable
        val bmp = when (dr) {
            is android.graphics.drawable.BitmapDrawable -> dr.bitmap
            is android.graphics.drawable.VectorDrawable -> dr.toBitmapCompat()
            else -> dr.toBitmapCompat()
        }
        val size = minOf(bmp.width, bmp.height)
        val left = (bmp.width - size) / 2
        val top = (bmp.height - size) / 2
        val square = Bitmap.createBitmap(bmp, left, top, size, size)
        square.asImageBitmap()
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


private fun makePreviewBitmap(size: Int = 720): android.graphics.Bitmap {
    val bmp = createBitmap(size, size)
    val c = android.graphics.Canvas(bmp)
    val p = android.graphics.Paint()
    val cell = size / 6
    for (r in 0 until 6) for (col in 0 until 6) {
        p.color = if ((r + col) % 2 == 0) 0xFFE0E0E0.toInt() else 0xFFBDBDBD.toInt()
        c.drawRect(
            (col * cell).toFloat(),
            (r * cell).toFloat(),
            ((col + 1) * cell).toFloat(),
            ((r + 1) * cell).toFloat(),
            p
        )
    }
    return bmp
}
