package com.divyadhun.app.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyadhun.app.LocaleController
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.Deity
import com.divyadhun.app.domain.model.Language
import com.divyadhun.app.domain.model.ListeningTime
import com.divyadhun.app.ui.DeityArtwork
import com.divyadhun.app.ui.DivyaButton
import com.divyadhun.app.ui.deityNameRes
import com.divyadhun.app.ui.listeningTimeNameRes
import com.divyadhun.app.ui.theme.LocalDevotionalColors
import com.divyadhun.app.ui.theme.Space

@Composable
fun OnboardingFlow(viewModel: OnboardingViewModel, deities: List<Deity>) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    if (!state.loaded) {
        Surface(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        return
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = Space.lg, vertical = Space.md).testTag("screen_ONBOARDING"),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    if (state.step > 0) {
                        OutlinedButton(onClick = viewModel::back, modifier = Modifier.testTag("onboarding_back").height(48.dp), contentPadding = PaddingValues(horizontal = Space.sm)) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                            Spacer(Modifier.width(Space.xs))
                            Text(stringResource(R.string.back))
                        }
                    } else Spacer(Modifier.size(48.dp))
                    Text(stringResource(R.string.brand_hindi), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("${state.step + 1}/4", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(Space.lg))
                StepProgress(state.step)
                Spacer(Modifier.height(Space.xl))
                when (state.step) {
                    0 -> WelcomeStep()
                    1 -> LanguageStep(state.language, viewModel::selectLanguage, context)
                    2 -> DeityStep(deities, state.selectedDeityIds, viewModel::toggleDeity)
                    else -> ListeningTimeStep(state.listeningTime, viewModel::selectListeningTime)
                }
            }
            Column(Modifier.fillMaxWidth().padding(top = Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                if (state.error) Text(stringResource(R.string.preference_error), color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("onboarding_error"))
                when (state.step) {
                    0 -> DivyaButton(stringResource(R.string.get_started), viewModel::next, modifier = Modifier.testTag("onboarding_get_started"))
                    1, 2 -> DivyaButton(stringResource(R.string.continue_label), viewModel::next, modifier = Modifier.testTag("onboarding_continue"))
                    else -> {
                        DivyaButton(stringResource(R.string.finish), onClick = {
                            viewModel.complete {
                                LocaleController.apply(context, state.language)
                            }
                        }, modifier = Modifier.testTag("onboarding_finish"))
                    }
                }
                if (state.step == 2 || state.step == 3) {
                    OutlinedButton(onClick = {
                        if (state.step == 2) viewModel.next() else {
                            viewModel.selectListeningTime(ListeningTime.ANYTIME)
                            viewModel.complete { LocaleController.apply(context, state.language) }
                        }
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("onboarding_skip")) { Text(stringResource(R.string.skip)) }
                }
            }
        }
    }
}

@Composable private fun StepProgress(step: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
        repeat(4) { index ->
            Surface(
                Modifier.weight(1f).height(4.dp),
                color = if (index <= step) LocalDevotionalColors.current.accent else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {}
        }
    }
}

@Composable private fun WelcomeStep() {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
        Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.onboarding_welcome_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
            Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.md)) {
                Text(stringResource(R.string.brand_hindi), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.tagline), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.secondary_tagline), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable private fun LanguageStep(language: Language, select: (Language, () -> Unit) -> Unit, context: android.content.Context) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.md)) {
        Text(stringResource(R.string.choose_language), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.choose_language_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ChoiceCard(stringResource(R.string.language_english), language == Language.ENGLISH, "onboarding_language_ENGLISH") { select(Language.ENGLISH) { LocaleController.apply(context, Language.ENGLISH) } }
        ChoiceCard(stringResource(R.string.language_hindi), language == Language.HINDI, "onboarding_language_HINDI") { select(Language.HINDI) { LocaleController.apply(context, Language.HINDI) } }
    }
}

@Composable private fun DeityStep(deities: List<Deity>, selected: Set<String>, toggle: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Space.md)) {
        Text(stringResource(R.string.deity_question), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.deity_question_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(pluralStringResource(R.plurals.selected_count, selected.size, selected.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        deities.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                pair.forEach { deity ->
                    DeityChoiceCard(deity, deity.id in selected, toggle, Modifier.weight(1f))
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun DeityChoiceCard(deity: Deity, isSelected: Boolean, toggle: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier.selectable(selected = isSelected, onClick = { toggle(deity.id) }, role = Role.Checkbox).semantics { selected = isSelected }.testTag("onboarding_deity_${deity.id}"),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))
    ) {
        Column(Modifier.fillMaxWidth().padding(Space.sm), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Space.xs)) {
            DeityArtwork(deity, Modifier.size(56.dp))
            Text(stringResource(deityNameRes(deity.id)), style = MaterialTheme.typography.titleSmall)
            if (isSelected) Icon(Icons.Outlined.Check, contentDescription = pluralStringResource(R.plurals.selected_count, 1, 1), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable private fun ListeningTimeStep(time: ListeningTime, select: (ListeningTime) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.md)) {
        Text(stringResource(R.string.time_question), style = MaterialTheme.typography.headlineLarge)
        Text(stringResource(R.string.time_question_body), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ListeningTime.entries.forEach { option ->
            ChoiceCard(stringResource(listeningTimeNameRes(option)), option == time, "onboarding_time_${option.name}") { select(option) }
        }
    }
}

@Composable private fun ChoiceCard(label: String, isSelected: Boolean, tag: String, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton).semantics { selected = isSelected }.testTag(tag),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .52f))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = Space.md, vertical = Space.sm), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isSelected, onClick = null)
            Spacer(Modifier.width(Space.sm))
            Text(label, style = MaterialTheme.typography.titleMedium)
        }
    }
}
