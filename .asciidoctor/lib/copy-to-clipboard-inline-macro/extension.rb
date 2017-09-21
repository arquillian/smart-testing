require 'asciidoctor/extensions' unless RUBY_ENGINE == 'opal'
require 'base64'

include ::Asciidoctor

# A block macro that embeds a copy to clipboard icon and copy the content identified by given id (target)
#
# Usage
#
#    [[run]]
#    ....
#    mvn clean test
#    ....
#
#    You can copy previous script copyToClipboard:run[]
#
#    This is an important token pass:q[<span id="token">`1234`</span>]copyToClipboard:token[] for you.
class ClipboardInlineMacro < Extensions::InlineMacroProcessor
  use_dsl

  named :copyToClipboard

  def process parent, target, attrs

    label = ""
    if attrs.has_key? 'label'
      label = attrs['label']
    end

    extdir = ::File.join(::File.dirname __FILE__)
    icon_name = 'clippy.svg'

    icon_location = %(#{extdir}/#{icon_name})
    %(
        <button class="cpybtn" width="13" data-clipboard-action="copy" data-clipboard-target="##{target}">#{label}
          <img src="#{ClipboardInlineMacro::img64(icon_location)}" width="13"/>
        </button>
    )
  end

  def self.img64(path)
    File.open(path, 'rb') do |img|
      'data:image/svg+xml;base64,' + Base64.strict_encode64(img.read)
    end
  end

end

class ClipboardAssetsDocinfoProcessor < Extensions::DocinfoProcessor
  use_dsl
  at_location :footer

  def process doc

    clipboard = %(
      var clipboard = new Clipboard('.cpybtn');
      clipboard.on('success', function(e) {
        console.log('Copy To Clipboard ' + e.text);
      });
      clipboard.on('error', function(e) {
        console.error('Copy To Clipboard ' + e.action);
      });
    )

    extdir = ::File.join(::File.dirname __FILE__)
    js_name = 'clipboard.min.js'

    content = doc.read_asset %(#{extdir}/#{js_name})
    ['<script>', content.chomp, '</script>', '<script>', clipboard.chomp, '</script>'] * "\n"

  end

end
