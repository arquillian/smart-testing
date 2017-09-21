RUBY_ENGINE == 'opal' ? (require 'copy-to-clipboard-inline-macro/extension') : (require_relative 'copy-to-clipboard-inline-macro/extension')

Extensions.register do
  inline_macro ClipboardInlineMacro
  docinfo_processor ClipboardAssetsDocinfoProcessor
end
