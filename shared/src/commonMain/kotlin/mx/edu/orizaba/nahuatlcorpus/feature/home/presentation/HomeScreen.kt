package mx.edu.orizaba.nahuatlcorpus.feature.home.presentation



import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.edu.orizaba.nahuatlcorpus.ui.theme.CorpusTheme
import nahuatlcorpus.shared.generated.resources.Res
import nahuatlcorpus.shared.generated.resources.corpus_header
import nahuatlcorpus.shared.generated.resources.welcome_create_account
import nahuatlcorpus.shared.generated.resources.welcome_sign_in
import nahuatlcorpus.shared.generated.resources.welcome_subtitle
import nahuatlcorpus.shared.generated.resources.welcome_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Preview(name = "Header artwork", widthDp = 390, heightDp = 320)
@Composable
private fun HeaderArtworkPreview(){
    CorpusTheme {
        HeaderArtwork(Modifier.fillMaxWidth())
    }
}

@Composable
private fun HeaderArtwork(modifier: Modifier = Modifier){
    Box(modifier = modifier,
        contentAlignment = Alignment.Center){
        Surface(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ){
            Image(
                painter =painterResource(Res.drawable.corpus_header),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.padding(16.dp).fillMaxWidth().aspectRatio(1521f /1034f),
            )
        }
    }
}