<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    value: string
    tone?: 'default' | 'positive' | 'negative'
  }>(),
  {
    tone: 'default'
  }
)

const TONE_CLASS: Record<string, string> = {
  default: 'text-[var(--color-text)]',
  positive: 'text-blue-600',
  negative: 'text-red-600'
}
</script>

<template>
  <div class="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
    <p class="text-sm text-[var(--color-text-muted)]">{{ label }}</p>
    <p class="mt-1 text-xl font-semibold" :class="TONE_CLASS[tone]">{{ value }}</p>
  </div>
</template>
