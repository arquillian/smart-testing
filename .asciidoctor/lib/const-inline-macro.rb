RUBY_ENGINE == 'opal' ? (require 'const-inline-macro/extension') : (require_relative 'const-inline-macro/extension')

Extensions.register do
  inline_macro ConstBlockMacro
end
