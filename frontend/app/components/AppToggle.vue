<script setup lang="ts">
defineProps<{
  modelValue: boolean
  label?: string
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <label
    class="flex items-center gap-3 text-sm text-[var(--color-text)]"
    :class="disabled ? 'opacity-50' : 'cursor-pointer'"
  >
    <button
      type="button"
      role="switch"
      :aria-checked="modelValue"
      :disabled="disabled"
      class="relative h-5 w-9 rounded-full transition-colors disabled:cursor-not-allowed"
      :class="modelValue ? 'bg-[var(--color-primary)]' : 'bg-gray-300'"
      @click="!disabled && $emit('update:modelValue', !modelValue)"
    >
      <span
        class="absolute left-0.5 top-0.5 h-4 w-4 rounded-full bg-white transition-transform"
        :class="modelValue ? 'translate-x-4' : 'translate-x-0'"
      />
    </button>
    <span v-if="label">{{ label }}</span>
  </label>
</template>
